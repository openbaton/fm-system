package org.openbaton.faultmanagement.system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.hibernate.validator.constraints.NotEmpty;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by mob on 16.12.15.
 */
@Service
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties
public class SystemStartup implements CommandLineRunner {

    private static final String name = "FaultManagementSystem";
    private Gson mapper;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String unsubscriptionIdINSTANTIATE_FINISH;
    private String unsubscriptionIdRELEASE_RESOURCES_FINISH;
    @Value("${server.port:}")
    @NotEmpty
    private String fmsPort;

    @Override
    public void run(String... args) throws Exception {
        GsonBuilder builder = new GsonBuilder();

        this.mapper = builder.setPrettyPrinting().create();
        InputStream is = new FileInputStream("/etc/openbaton/openbaton.properties");
        Properties properties = new Properties();
        properties.load(is);
        String nfvoIp = properties.getProperty("nfvo.publicIp");
        String nfvoPort = properties.getProperty("server.port","8080");
        log.info("NFVO ip: "+nfvoIp);
        log.info("NFVO port: "+nfvoPort);
        log.info("FMS port: "+ fmsPort);


        String nfvoUrlEvent = "http://"+nfvoIp+":"+nfvoPort+"/api/v1/events";
        String fmsIp=nfvoIp;
        EventEndpoint eventEndpointInstantiateFinish = createEventEndpoint(name, EndpointType.REST, Action.INSTANTIATE_FINISH,"http://"+fmsIp+":"+ fmsPort +"/nfvo/events");
        EventEndpoint eventEndpointReleaseResourcesFinish = createEventEndpoint(name,EndpointType.REST,Action.RELEASE_RESOURCES_FINISH,"http://"+fmsIp+":"+ fmsPort +"/nfvo/events");

        String eventEndpointJson=mapper.toJson(eventEndpointInstantiateFinish);
        HttpResponse<JsonNode> jsonResponse=null;
        /*try {
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
        log.info("Correctly registered to the NFVO");*/
    }
    private EventEndpoint createEventEndpoint(String name, EndpointType type, Action action,String url){
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(action);
        eventEndpoint.setName(name);
        eventEndpoint.setType(type);
        eventEndpoint.setEndpoint(url);
        return eventEndpoint;
    }
}
