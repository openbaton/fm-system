package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.net.httpserver.HttpServer;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.monitoring.ObjectSelection;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/*import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;*/

/**
 * Created by mob on 26.10.15.
 */
@Service
public class NSRManager {
    private HttpServer server;
    private Set<NetworkServiceRecord> nsrSet;
    private static final String name = "FaultManagement";
    private Gson mapper;
    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    private String unsubscriptionIdINSTANTIATE_FINISH;
    private String unsubscriptionIdRELEASE_RESOURCES_FINISH;
    private List<NetworkServiceRecord> nsrList;

    //private NFVORequestor nfvoRequestor;
    @Autowired
    private PolicyManager policyManager;

    private ObjectSelection getObjectSelector(){
        ObjectSelection objectSelection =new ObjectSelection();
        objectSelection.addObjectInstanceId("iperf-client-110");
        objectSelection.addObjectInstanceId("iperf-server-820");
        return objectSelection;
    }
    private List<String> getPerformanceMetrics(){
        List<String> performanceMetrics= new ArrayList<>();
        performanceMetrics.add("net.tcp.listen[5001]");
        return performanceMetrics;
    }
    @PreDestroy
    public void stopNSMonitoring(){
        for (NetworkServiceRecord nsr : nsrList){
            try {
                policyManager.unManageNSR(nsr);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }
        }
    }
    @PostConstruct
    public void init() throws IOException/*, SDKException*/ {
        /*Properties properties=new Properties();
        properties.load(new FileInputStream("fm.properties"));*/
        nsrSet=new HashSet<>();
        log.debug("NSRManager started");

        // returns an array of TypeVariable object
        GsonBuilder builder = new GsonBuilder();
        /*builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });*/
        this.mapper = builder.setPrettyPrinting().create();

        ObjectSelection objectSelection = getObjectSelector();
        List<String> performanceMetrics=getPerformanceMetrics();
        String url="http://localhost:8080/api/v1/ns-records";
        HttpResponse<JsonNode> jsonResponse=null;
        try {
            jsonResponse = Unirest.get(url).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

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

        try {
            policyManager.manageNSR(nsrList.iterator().next());
        } catch (FaultManagementPolicyException e) {
            log.error(e.getMessage(),e);
        }
        //REGISTRATION TO NFVO
        //nfvoRequestor = new NFVORequestor(properties.getProperty("nfvo-usr"),properties.getProperty("nfvo-pwd"), properties.getProperty("nfvo-ip"),properties.getProperty("nfvo-port"),"1");

        /*EventEndpoint eventEndpoint= createEventEndpoint();
        EventEndpoint response = null;
        try {
            response = nfvoRequestor.getEventAgent().create(eventEndpoint);
            if (response == null)
                throw new NullPointerException("Response is null");
            unsubscriptionIdINSTANTIATE_FINISH=response.getId();
            eventEndpoint.setEvent(Action.RELEASE_RESOURCES_FINISH);

            response = nfvoRequestor.getEventAgent().create(eventEndpoint);
            if (response == null)
                throw new NullPointerException("Response is null");
            unsubscriptionIdRELEASE_RESOURCES_FINISH=response.getId();
        } catch (SDKException e) {
            log.error("Subscription failed for the NSRs");
            throw e;
        }*/
    }


    public Set<NetworkServiceRecord> getNsrSet() {
        return nsrSet;
    }

    private EventEndpoint createEventEndpoint(){
        EventEndpoint eventEndpoint= new EventEndpoint();
        eventEndpoint.setName(name);
        eventEndpoint.setEvent(Action.INSTANTIATE_FINISH);
        eventEndpoint.setType(EndpointType.REST);
        String url = "http://localhost:" + server.getAddress().getPort() + "/" + name;
        eventEndpoint.setEndpoint(url);
        return eventEndpoint;
    }

    private boolean checkRequest(String message) {
        /*JsonElement jsonElement = Mapper.getMapper().fromJson(message, JsonElement.class);

        String actionReceived= jsonElement.getAsJsonObject().get("action").getAsString();
        log.debug("Action received: " + actionReceived);
        Action action=Action.valueOf(actionReceived);
        String payload= jsonElement.getAsJsonObject().get("payload").getAsString();
        log.debug("Payload received: " + payload);
        NetworkServiceRecord nsr=null;
        try {
            nsr = Mapper.getMapper().fromJson(payload, NetworkServiceRecord.class);
        }catch (Exception e){
            log.warn("Impossible to retrive the NSR received",e);
            return false;
        }
        if(action.ordinal()==Action.INSTANTIATE_FINISH.ordinal()) {
            nsrSet.add(nsr);
            try {
                policyManager.manageNSR(nsr);
            } catch (FaultManagementPolicyException e) {
                log.error("Policy manager Exception", e);
            }
        }
        else if(action.ordinal()==Action.RELEASE_RESOURCES_FINISH.ordinal()){
            nsrSet.remove(nsr);
            policyManager.unManageNSR(nsr.getId());
        }
        else {
            log.debug("Action unknow: "+action);
            return false;
        }*/
        return true;
    }
}
