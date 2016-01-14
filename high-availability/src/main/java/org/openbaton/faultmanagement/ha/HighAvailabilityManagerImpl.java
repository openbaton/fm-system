package org.openbaton.faultmanagement.ha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    public void switchToRedundantVNFC(VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu,VNFCInstance vnfcInstance) throws HighAvailabilityException {
        try {
            sendSwitchToStandbyMessage(vnfr.getParent_ns_id(), vnfr.getId(), vdu.getId(),vnfcInstance.getId());
        } catch (UnirestException e) {
            throw new HighAvailabilityException(e.getMessage(),e);
        }
    }

    public void configureRedundancy(VirtualNetworkFunctionRecord vnfr) throws HighAvailabilityException {
        if(!vnfrNeedsRedundancy(vnfr))
            return;
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
            if (vdu.getHigh_availability().getResiliencyLevel().ordinal() == ResiliencyLevel.ACTIVE_STANDBY_STATELESS.ordinal()) {
                if (vdu.getHigh_availability().getRedundancyScheme().equals("1:N")) {
                    log.debug("VNFC COMPONENTS:\n"+vdu.getVnfc().toString());
                    createStandByVNFC(vdu.getVnfc().iterator().next(), vnfr, vdu);
                }
            }
        }

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
    private void sendSwitchToStandbyMessage(String ... ids) throws UnirestException {

        String finalUrl=nfvoUrl;
        finalUrl += "/"+ids[0];
        finalUrl += ids[1]==null ? "" : "/vnfrecords/"+ids[1];
        finalUrl += ids[2]==null ? "" : "/vdunits/"+ids[2];
        finalUrl += ids[3]==null ? "" : "/vnfcinstances/"+ids[3];
        finalUrl += "/switchtostandby";

        HttpResponse<String> jsonResponse;
        log.debug("Posting new VNFC in standby mode: "+finalUrl);

        jsonResponse = Unirest.post(finalUrl).header("Content-type","application/json").header("KeepAliveTimeout","5000").asString();

        log.debug("Response from nfvo: "+jsonResponse.getBody());
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

        log.debug("Response from nfvo: "+jsonResponse.getBody());
    }
}
