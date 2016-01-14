package org.openbaton.faultmanagement.fc;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.net.httpserver.HttpServer;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        log.debug("NSRManager started");

        this.mapper = new GsonBuilder().setPrettyPrinting().create();
        InputStream is = new FileInputStream("/etc/openbaton/openbaton.properties");
        Properties properties = new Properties();
        properties.load(is);
        nfvoIp = properties.getProperty("nfvo.publicIp");
        nfvoPort = properties.getProperty("server.port","8080");
        nfvoUrl = "http://"+nfvoIp+":"+nfvoPort+"/api/v1/ns-records";

        /*VirtualNetworkFunctionRecord vnfr = getVirtualNetworkFunctionRecord("60a52def-ae27-4927-932a-a78cf6130a1a","00be8f38-f721-463b-bf7b-3cc39222e3d9");
        log.debug("------------The vnfr is: "+vnfr.toString());
        for(VirtualDeploymentUnit vdu: vnfr.getVdu()){
            for(VNFCInstance vnfcInstance : vdu.getVnfc_instance())
                    log.debug("....."+vnfcInstance);
        }*/

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
            log.debug("Nsr name: "+nsr.getName() +" nsr id:"+nsr.getId());
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                log.debug("Vnfr name: "+vnfr.getName());
                if(vnfr.getFault_management_policy()!=null)
                    for(VNFFaultManagementPolicy fmp: vnfr.getFault_management_policy()){
                        log.debug("fmpolicy: "+fmp);
                    }
            }
        }

        return nsrList;
    }

    @Override
    public NetworkServiceRecord getNetworkServiceRecord(String nsrId) {
        return this.getNetworkServiceRecordFromNfvo(nfvoUrl+"/"+nsrId);
    }

    private NetworkServiceRecord getNetworkServiceRecordFromNfvo(String url) {
        HttpResponse<JsonNode> jsonResponse = executeGet(url);
        return mapper.fromJson(jsonResponse.getBody().toString(), NetworkServiceRecord.class);
    }
    @Override
    public List<NetworkServiceRecord> getNetworkServiceRecords() {
        return this.getNetworkServiceRecordsFromNfvo(nfvoUrl);
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) {
        HttpResponse<JsonNode> jsonResponse = executeGet(nfvoUrl+"/"+nsrId+"/vnfrecords/"+vnfrId);
        return mapper.fromJson(jsonResponse.getBody().toString(), VirtualNetworkFunctionRecord.class);
    }
}
