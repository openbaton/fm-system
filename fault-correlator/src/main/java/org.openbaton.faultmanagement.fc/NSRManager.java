package org.openbaton.faultmanagement.fc;

import com.google.gson.*;
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
    private String fmsIp,fmsPort;
    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    private String unsubscriptionIdINSTANTIATE_FINISH;
    private String unsubscriptionIdRELEASE_RESOURCES_FINISH;
    private List<NetworkServiceRecord> nsrList;

    //private NFVORequestor nfvoRequestor;
    @Autowired
    private PolicyManager policyManager;

    @PostConstruct
    public void init() throws IOException {
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


        String nfvoUrlRecords="http://localhost:8080/api/v1/ns-records";

        //List<NetworkServiceRecord> nsrList = getNetworkServiceRecords(nfvoUrl);

        /*try {
            policyManager.manageNSR(nsrList.iterator().next());
        } catch (FaultManagementPolicyException e) {
            log.error(e.getMessage(),e);
        }*/

        String nfvoUrlEvent="http://localhost:8080/api/v1/events";
        fmsIp="http://localhost";
        fmsPort="9000";
        EventEndpoint eventEndpointInstantiateFinish = createEventEndpoint(name,EndpointType.REST,Action.INSTANTIATE_FINISH,fmsIp+":"+fmsPort+"/nfvo/event");
        EventEndpoint eventEndpointReleaseResourcesFinish = createEventEndpoint(name,EndpointType.REST,Action.RELEASE_RESOURCES_FINISH,fmsIp+":"+fmsPort+"/nfvo/event");

        String eventEndpointJson=mapper.toJson(eventEndpointInstantiateFinish);
        HttpResponse<JsonNode> jsonResponse=null;
        try {
            jsonResponse = Unirest.post(nfvoUrlEvent).header("accept", "application/json").header("Content-Type", "application/json").body(eventEndpointJson).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        EventEndpoint response= mapper.fromJson(jsonResponse.getBody().toString(),EventEndpoint.class);
        unsubscriptionIdINSTANTIATE_FINISH = response.getId();

        eventEndpointJson=mapper.toJson(eventEndpointReleaseResourcesFinish);
        try {
            jsonResponse = Unirest.post(nfvoUrlEvent).header("accept", "application/json").header("Content-Type", "application/json").body(eventEndpointJson).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        response= mapper.fromJson(jsonResponse.getBody().toString(),EventEndpoint.class);
        unsubscriptionIdRELEASE_RESOURCES_FINISH = response.getId();
    }

    private List<NetworkServiceRecord> getNetworkServiceRecords(String url){
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

        return nsrList;
    }

    protected EventEndpoint createEventEndpoint(String name, EndpointType type, Action action,String url){
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(action);
        eventEndpoint.setName(name);
        eventEndpoint.setType(type);
        eventEndpoint.setEndpoint(url);
        return eventEndpoint;
    }
}
