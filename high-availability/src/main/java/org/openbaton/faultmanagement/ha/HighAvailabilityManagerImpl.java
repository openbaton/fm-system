package org.openbaton.faultmanagement.ha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by mob on 11.01.16.
 */
@Service
public class HighAvailabilityManagerImpl implements HighAvailabilityManager {
    private Gson mapper;
    private static final Logger log = LoggerFactory.getLogger(HighAvailabilityManagerImpl.class);
    private String nfvoIp,nfvoPort,nfvoUrl;

    @PostConstruct
    public void init() throws IOException {
        mapper = new GsonBuilder().setPrettyPrinting().create();
        InputStream is = new FileInputStream("/etc/openbaton/openbaton.properties");
        Properties properties = new Properties();
        properties.load(is);
        nfvoIp = properties.getProperty("nfvo.publicIp");
        nfvoPort = properties.getProperty("server.port","8080");
        nfvoUrl = "http://"+nfvoIp+":"+nfvoPort+"/api/v1/ns-records";
    }

    public void switchToRedundantVNFC(VNFCInstance failedVnfcInstance,String nsrId, String vnfrId, String vduId,String vnfcInstanceId) throws HighAvailabilityException {
        try {
            sendSwitchToStandbyMessage(failedVnfcInstance,nsrId, vnfrId, vduId,vnfcInstanceId);
        } catch (UnirestException e) {
            throw new HighAvailabilityException(e.getMessage(),e);
        }
    }
    public void switchToRedundantVNFC(VNFCInstance failedVnfcInstance, VirtualNetworkFunctionRecord vnfr,VirtualDeploymentUnit vdu) throws HighAvailabilityException {

        for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
            if(vnfcInstance.getState()!=null && vnfcInstance.getState().equals("standby"))
                switchToRedundantVNFC(failedVnfcInstance,vnfr.getParent_ns_id(),vnfr.getId(),vdu.getId(),vnfcInstance.getId());
        }
    }

    public void executeHeal(String cause, String nsrId, String vnfrId, String vduId, String vnfcInstanceId){
        VnfmOrHealedMessage healMessage = getHealMessage(cause);
        sendHealMessage(healMessage, nsrId, vnfrId, vduId, vnfcInstanceId);
    }

    private VnfmOrHealedMessage getHealMessage(String cause) {
        VnfmOrHealedMessage vnfmOrHealVNFRequestMessage = new VnfmOrHealedMessage();
        vnfmOrHealVNFRequestMessage.setAction(Action.HEAL);
        vnfmOrHealVNFRequestMessage.setCause(cause);
        return vnfmOrHealVNFRequestMessage;
    }
    private void sendHealMessage(VnfmOrHealedMessage healMessage,String ... ids) {
        HttpResponse<String> jsonResponse=null;
        String finalUrl=nfvoUrl;
        finalUrl += "/"+ids[0];
        finalUrl += ids[1]==null ? "" : "/vnfrecords/"+ids[1];
        finalUrl += ids[2]==null ? "" : "/vdunits/"+ids[2];
        finalUrl += ids[3]==null ? "" : "/vnfcinstances/"+ids[3];
        finalUrl+="/actions";
        log.debug("Posting action heal at url: "+finalUrl);
        String jsonMessage= mapper.toJson(healMessage,VnfmOrHealedMessage.class);
        try {
            jsonResponse = Unirest.post(finalUrl).header("Content-type","application/json").header("KeepAliveTimeout","5000").body(jsonMessage).asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        log.debug("Response status from nfvo: "+jsonResponse.getCode());
    }
    public void configureRedundancy(VirtualNetworkFunctionRecord vnfr) throws HighAvailabilityException, UnirestException {
        if(!vnfrNeedsRedundancy(vnfr))
            return;

        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
            if (vdu.getHigh_availability().getResiliencyLevel().ordinal() == ResiliencyLevel.ACTIVE_STANDBY_STATELESS.ordinal()) {
                if (vdu.getHigh_availability().getRedundancyScheme().equals("1:N")) {
                    // check the 1:N redundancy
                    if(checkIfStandbyVNFCInstance(vdu))
                        continue;

                    if( checkMaxNumInstances(vdu) )
                        continue;

                    //log.debug("VNFC COMPONENTS:\n"+vdu.getVnfc().toString());
                    //log.debug("VNFC INSTANCES:\n"+vdu.getVnfc_instance());

                    //Get a component sample
                    VNFComponent componentSample = vdu.getVnfc().iterator().next();

                    //Creating a new component to add into the vdu
                    VNFComponent vnfComponent_new = new VNFComponent();
                    Set<VNFDConnectionPoint> vnfdConnectionPointSet= new HashSet<>();
                    for (VNFDConnectionPoint vnfdConnectionPointSample: componentSample.getConnection_point()){
                        VNFDConnectionPoint vnfdConnectionPoint = new VNFDConnectionPoint();
                        vnfdConnectionPoint.setVirtual_link_reference(vnfdConnectionPointSample.getVirtual_link_reference());
                        vnfdConnectionPoint.setFloatingIp(vnfdConnectionPointSample.getFloatingIp());
                        vnfdConnectionPoint.setType(vnfdConnectionPointSample.getType());

                        vnfdConnectionPointSet.add(vnfdConnectionPoint);
                    }
                    vnfComponent_new.setConnection_point(vnfdConnectionPointSet);

                    try {
                        createStandByVNFC(vnfComponent_new, vnfr, vdu);
                    } catch (HighAvailabilityException e) {
                        log.error(e.getMessage(),e);
                    }
                }
            }
        }
    }

    public String cleanFailedInstances(VirtualNetworkFunctionRecord vnfr) throws UnirestException {
        for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
            for(VNFCInstance vnfcInstance: vdu.getVnfc_instance()){
                if(vnfcInstance.getState()!=null && vnfcInstance.getState().equals("failed")){
                    log.debug("The vnfcInstance: "+ vnfcInstance.getHostname() +" of the vnfr: "+ vnfr.getName()+" is in "+vnfcInstance.getState()+" state");
                    sendScaleInMessage(vnfr.getParent_ns_id(),vnfr.getId(),vdu.getId(),vnfcInstance.getId());
                    return vnfcInstance.getHostname();
                }
            }
        }
        return null;
    }

    private boolean checkMaxNumInstances(VirtualDeploymentUnit vdu) {
        if(vdu.getScale_in_out() == vdu.getVnfc_instance().size()){
            log.warn("The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance. So, no VNFC in stanby can be created");
            return true;
        }
        return false;
    }

    private boolean checkIfStandbyVNFCInstance(VirtualDeploymentUnit vdu) {
        for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
            if(vnfcInstance.getState()!=null && vnfcInstance.getState().equals("standby"))
                return true;
        }
        return false;
    }

    private boolean vnfrNeedsRedundancy(VirtualNetworkFunctionRecord vnfr) {

        for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
            if(vdu.getHigh_availability()!=null && vdu.getHigh_availability().getRedundancyScheme()!=null && !vdu.getHigh_availability().getRedundancyScheme().isEmpty()
                    && vdu.getHigh_availability().getResiliencyLevel()!=null)
                return true;
        }
        return false;
    }

    public void createStandByVNFC(VNFComponent vnfComponent, VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu) throws HighAvailabilityException {

        try {
            sendAddVNFCMessage(vnfComponent, vnfr.getParent_ns_id(), vnfr.getId(), vdu.getId());
        } catch (UnirestException e) {
            throw new HighAvailabilityException(e.getMessage(),e);
        }
    }
    private void sendScaleInMessage(String ... ids) throws UnirestException {

        String finalUrl=nfvoUrl;
        finalUrl += "/"+ids[0];
        finalUrl += ids[1]==null ? "" : "/vnfrecords/"+ids[1];
        finalUrl += ids[2]==null ? "" : "/vdunits/"+ids[2];
        finalUrl += ids[3]==null ? "" : "/vnfcinstances/"+ids[3];

        HttpResponse<String> jsonResponse;
        log.debug("Delete message to this url: "+finalUrl);
        jsonResponse = Unirest.delete(finalUrl).header("KeepAliveTimeout","5000").asString();

        log.debug("Response status from nfvo: "+jsonResponse.getCode());
    }
    private void sendSwitchToStandbyMessage(VNFCInstance failedVnfcInstance,String ... ids) throws UnirestException {

        String finalUrl=nfvoUrl;
        finalUrl += "/"+ids[0];
        finalUrl += ids[1]==null ? "" : "/vnfrecords/"+ids[1];
        finalUrl += ids[2]==null ? "" : "/vdunits/"+ids[2];
        finalUrl += ids[3]==null ? "" : "/vnfcinstances/"+ids[3];
        finalUrl += "/switchtostandby";

        HttpResponse<String> jsonResponse;
        log.debug("Sending message switch to standby: "+finalUrl);
        String jsonMessage= mapper.toJson(failedVnfcInstance,VNFCInstance.class);
        jsonResponse = Unirest.post(finalUrl).header("Content-type","application/json").header("KeepAliveTimeout","5000").body(jsonMessage).asString();

        log.debug("Response status from nfvo: "+jsonResponse.getCode());
    }

    private void sendAddVNFCMessage(VNFComponent vnfComponent, String ... ids) throws UnirestException {

        String finalUrl=nfvoUrl;
        finalUrl += "/"+ids[0];
        finalUrl += ids[1]==null ? "" : "/vnfrecords/"+ids[1];
        finalUrl += ids[2]==null ? "" : "/vdunits/"+ids[2];
        finalUrl += "/vnfcinstances/standby";

        HttpResponse<String> jsonResponse;
        log.debug("Posting new VNFC in standby mode: "+finalUrl);
        String jsonMessage= mapper.toJson(vnfComponent,VNFComponent.class);

        jsonResponse = Unirest.post(finalUrl).header("Content-type","application/json").header("KeepAliveTimeout","5000").body(jsonMessage).asString();
        log.debug("Response status from nfvo: "+jsonResponse.getCode());
    }
}
