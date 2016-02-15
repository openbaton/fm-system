package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.net.httpserver.HttpServer;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/*import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exceptions.SDKException;*/

/**
 * Created by mob on 26.10.15.
 */
@Service
public class NSRManagerImpl implements NSRManager {
    private HttpServer server;
    private Set<NetworkServiceRecord> nsrSet;
    private static final String name = "FaultManagement";
    private Gson mapper;
    private String fmsIp,fmsPort;
    private static final Logger log = LoggerFactory.getLogger(NSRManagerImpl.class);
    private String unsubscriptionIdINSTANTIATE_FINISH;
    private String unsubscriptionIdRELEASE_RESOURCES_FINISH;
    private List<NetworkServiceRecord> nsrList;
    private String nfvoIp,nfvoPort,nfvoUrl;
    //private NFVORequestor nfvoRequestor;
    @Autowired
    private PolicyManager policyManager;

    @PostConstruct
    public void init() throws IOException {
        /*Properties properties=new Properties();
        properties.load(new FileInputStream("fm.properties"));*/
        nsrSet=new HashSet<>();

        this.mapper = new GsonBuilder().setPrettyPrinting().create();
        InputStream is = new FileInputStream("/etc/openbaton/openbaton.properties");
        Properties properties = new Properties();
        properties.load(is);
        nfvoIp = properties.getProperty("nfvo.publicIp");
        nfvoPort = properties.getProperty("server.port","8080");
        nfvoUrl = "http://"+nfvoIp+":"+nfvoPort+"/api/v1/ns-records";
    }

    private HttpResponse<JsonNode> executeGet(String url){
        HttpResponse<JsonNode> jsonResponse=null;
        try {
            jsonResponse = Unirest.get(url).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    private List<NetworkServiceRecord> getNetworkServiceRecordsFromNfvo(String url){

        HttpResponse<JsonNode> jsonResponse = executeGet(url);
        Class<?> aClass = Array.newInstance(NetworkServiceRecord.class, 3).getClass();
        Object[] nsrArray = (Object[]) mapper.fromJson(jsonResponse.getBody().toString(), aClass);
        nsrList = Arrays.asList((NetworkServiceRecord[]) nsrArray);
        for(NetworkServiceRecord nsr : nsrList){
            //log.debug("Nsr name: "+nsr.getName() +" nsr id:"+nsr.getId());
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                //log.debug("Vnfr name: "+vnfr.getName());
                for(VirtualDeploymentUnit vdu : vnfr.getVdu() )
                if(vdu.getFault_management_policy()!=null)
                    for(VRFaultManagementPolicy fmp: vdu.getFault_management_policy()){
                        //log.debug("fmpolicy: "+fmp);
                    }
            }
        }

        return nsrList;
    }

    @Override
    public NetworkServiceRecord getNetworkServiceRecord(String nsrId) throws NotFoundException {
        return this.getNetworkServiceRecordFromNfvo(nfvoUrl+"/"+nsrId);
    }

    private NetworkServiceRecord getNetworkServiceRecordFromNfvo(String url) throws NotFoundException {
        HttpResponse<JsonNode> jsonResponse = executeGet(url);
        if(jsonResponse.getBody()==null)
            throw new NotFoundException("Not possibile to retrieve the NSR from the orchestrator");
        return mapper.fromJson(jsonResponse.getBody().toString(), NetworkServiceRecord.class);
    }
    @Override
    public List<NetworkServiceRecord> getNetworkServiceRecords() {
        return this.getNetworkServiceRecordsFromNfvo(nfvoUrl);
    }



    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) throws NotFoundException {
        HttpResponse<JsonNode> jsonResponse = executeGet(nfvoUrl+"/"+nsrId+"/vnfrecords/"+vnfrId);
        if(jsonResponse.getBody()==null)
            throw new NotFoundException("Not possibile to retrieve the VNFR from the orchestrator");
        return mapper.fromJson(jsonResponse.getBody().toString(), VirtualNetworkFunctionRecord.class);
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId) {
        /*VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setEndpoint("generic");
        return virtualNetworkFunctionRecord;*/
        for(NetworkServiceRecord nsr : getNetworkServiceRecords()){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                if(vnfr.getId().equals(vnfrId))
                    return vnfr;
            }
        }
        return null;
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname) {
        /*VirtualNetworkFunctionRecord virtualNetworkFunctionRecord = new VirtualNetworkFunctionRecord();
        virtualNetworkFunctionRecord.setId("vnfrid");
        return virtualNetworkFunctionRecord;*/
        List<NetworkServiceRecord> nsrs= getNetworkServiceRecords();
        for(NetworkServiceRecord nsr : nsrs){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                        if(vnfcInstance.getHostname().equals(hostname))
                            return vnfr;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId) {
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                    if(vnfcInstance.getId().equals(vnfcInstaceId))
                        return vnfcInstance;
                }
            }
        return null;
    }

    public VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr,String vnfcInstaceId) {
       /*VirtualDeploymentUnit vdu = new VirtualDeploymentUnit();
        vdu.setVimInstanceName("vim-50");
        return vdu;*/
         for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
            for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                if(vnfcInstance.getId().equals(vnfcInstaceId))
                    return vdu;
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstance(String hostname) {
        /*//test
        VNFCInstance vnfcInstance = new VNFCInstance();
        vnfcInstance.setId("id1");
        vnfcInstance.setVim_id("vim id");
        vnfcInstance.setHostname("iperf 1");
        return vnfcInstance;
*/
        List<NetworkServiceRecord> nsrs= getNetworkServiceRecords();
        for(NetworkServiceRecord nsr : nsrs){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                        if(vnfcInstance.getHostname().equals(hostname))
                            return vnfcInstance;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstanceById(String VnfcId) {
        /*VNFCInstance vnfcInstance = new VNFCInstance();
        vnfcInstance.setId("id1");
        vnfcInstance.setVim_id("vim id");
        vnfcInstance.setHostname("iperf 1");
        return vnfcInstance;*/
        List<NetworkServiceRecord> nsrs= getNetworkServiceRecords();
        for(NetworkServiceRecord nsr : nsrs){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                        if(vnfcInstance.getId().equals(VnfcId))
                            return vnfcInstance;
                    }
                }
            }
        }
        return null;
    }
}
