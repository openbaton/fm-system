package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/*import org.openbaton.sdk.NFVORequestorWrapper;
import org.openbaton.sdk.api.exceptions.SDKException;*/

/**
 * Created by mob on 26.10.15.
 */
@Service
public class NFVORequestorWrapperWrapper implements org.openbaton.faultmanagement.fc.interfaces.NFVORequestorWrapper {
    private Set<NetworkServiceRecord> nsrSet;
    private static final String name = "FaultManagement";
    private Gson mapper;
    private static final Logger log = LoggerFactory.getLogger(NFVORequestorWrapperWrapper.class);
    private List<NetworkServiceRecord> nsrList;
    private String nfvoIp,nfvoPort,nfvoUrl;
    //private NFVORequestor nfvoRequestor;

    @PostConstruct
    public void init() throws IOException {
        nsrSet=new HashSet<>();

        this.mapper = new GsonBuilder().setPrettyPrinting().create();
        InputStream is = new FileInputStream("/etc/openbaton/openbaton.properties");
        Properties properties = new Properties();
        properties.load(is);
        nfvoIp = properties.getProperty("nfvo.rabbit.brokerIp");
        nfvoPort = properties.getProperty("server.port","8080");
        nfvoUrl = "http://"+nfvoIp+":"+nfvoPort+"/api/v1/ns-records";
        //nfvoRequestor = new NFVORequestor(properties.getProperty("nfvo-usr"),properties.getProperty("nfvo-pwd"), nfvoIp,nfvoPort,"1");
    }

    private HttpResponse<JsonNode> executeGet(String url) throws NFVORequestorException {
        HttpResponse<JsonNode> jsonResponse=null;
        try {
            jsonResponse = Unirest.get(url).asJson();
        } catch (UnirestException e) {
            throw new NFVORequestorException("Is not possible to retrieve data from NFVO");
        }
        return jsonResponse;
    }

    private List<NetworkServiceRecord> getNetworkServiceRecordsFromNfvo(String url) throws NFVORequestorException {

        HttpResponse<JsonNode> jsonResponse = executeGet(url);
        Class<?> aClass = Array.newInstance(NetworkServiceRecord.class, 3).getClass();
        Object[] nsrArray = (Object[]) mapper.fromJson(jsonResponse.getBody().toString(), aClass);
        nsrList = Arrays.asList((NetworkServiceRecord[]) nsrArray);
        return nsrList;
    }

    @Override
    public NetworkServiceRecord getNetworkServiceRecord(String nsrId) throws NotFoundException, NFVORequestorException {
        return this.getNetworkServiceRecordFromNfvo(nfvoUrl+"/"+nsrId);
    }

    private NetworkServiceRecord getNetworkServiceRecordFromNfvo(String url) throws NotFoundException, NFVORequestorException {
        HttpResponse<JsonNode> jsonResponse = executeGet(url);
        if(jsonResponse.getBody()==null)
            throw new NotFoundException("Not possibile to retrieve the NSR from the orchestrator");
        return mapper.fromJson(jsonResponse.getBody().toString(), NetworkServiceRecord.class);
    }
    @Override
    public List<NetworkServiceRecord> getNetworkServiceRecords() throws NFVORequestorException {
        return this.getNetworkServiceRecordsFromNfvo(nfvoUrl);
    }



    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) throws NotFoundException, NFVORequestorException {
        HttpResponse<JsonNode> jsonResponse = executeGet(nfvoUrl+"/"+nsrId+"/vnfrecords/"+vnfrId);
        if(jsonResponse.getBody()==null)
            throw new NotFoundException("Not possibile to retrieve the VNFR from the orchestrator");
        return mapper.fromJson(jsonResponse.getBody().toString(), VirtualNetworkFunctionRecord.class);
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId) throws NFVORequestorException {
        for(NetworkServiceRecord nsr : getNetworkServiceRecords()){
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                if(vnfr.getId().equals(vnfrId))
                    return vnfr;
            }
        }
        return null;
    }

    @Override
    public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname) throws NFVORequestorException {
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

         for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
            for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                if(vnfcInstance.getId().equals(vnfcInstaceId))
                    return vdu;
            }
        }
        return null;
    }

    @Override
    public VNFCInstance getVNFCInstance(String hostname) throws NFVORequestorException {

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
    public VNFCInstance getVNFCInstanceById(String VnfcId) throws NFVORequestorException {

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
