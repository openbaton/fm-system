package org.openbaton.faultmanagement;

import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.faultmanagement.parser.Parser;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by mob on 26.10.15.
 */
public class AbstractFaultMangement {
    private HttpServer server;
    private Set<NetworkServiceRecord> nsrSet;
    private MyHandler myHandler;
    private static final String name = "FaultManagement";
    private static final Logger log = LoggerFactory.getLogger(AbstractFaultMangement.class);
    private NFVORequestor nfvoRequestor;
    public static void main(String[] args) {
        SpringApplication.run(AbstractFaultMangement.class);
    }

    public void init() throws IOException {
        Properties properties=new Properties();
        properties.load(new FileInputStream("fm.properties"));
        nfvoRequestor = new NFVORequestor(properties.getProperty("nfvo-usr"),properties.getProperty("nfvo-pwd"), properties.getProperty("nfvo-ip"),properties.getProperty("nfvo-port"),"1");
        launchServer();
        nsrSet=new HashSet<>();
        EventEndpoint eventEndpoint= new EventEndpoint();
        eventEndpoint.setName(name);
        eventEndpoint.setEvent(Action.INSTANTIATE_FINISH);
        eventEndpoint.setType(EndpointType.REST);
        String url = "http://localhost:" + server.getAddress().getPort() + "/" + name;
        eventEndpoint.setEndpoint(url);


        EventEndpoint response = null;
        String unsubscriptionId1;
        String unsubscriptionId2;
        try {
            response = nfvoRequestor.getEventAgent().create(eventEndpoint);
            if (response == null)
                throw new NullPointerException("Response is null");
            unsubscriptionId1=response.getId();
            eventEndpoint.setEvent(Action.RELEASE_RESOURCES_FINISH);

            response = nfvoRequestor.getEventAgent().create(eventEndpoint);
            if (response == null)
                throw new NullPointerException("Response is null");
            unsubscriptionId2=response.getId();
        } catch (SDKException e) {
            log.error("Subscription failed for the NSRs");
            e.printStackTrace();
        }

    }
    private void launchServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 1);
        myHandler=new MyHandler();
        server.createContext("/" + name, myHandler);
        server.setExecutor(null);
        server.start();
    }
    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            String message = read(is);
            checkRequest(message);
            String response = "";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean checkRequest(String message) {
            JsonElement jsonElement = Parser.getMapper().fromJson(message, JsonElement.class);

            String actionReceived= jsonElement.getAsJsonObject().get("action").getAsString();
            log.debug("Action received: " + actionReceived);
            Action action=Action.valueOf(actionReceived);
            String payload= jsonElement.getAsJsonObject().get("payload").getAsString();
            log.debug("Payload received: "+payload);
            NetworkServiceRecord nsr=null;
            try {
                nsr = Parser.getMapper().fromJson(payload, NetworkServiceRecord.class);
            }catch (Exception e){
                log.warn("Impossible to retrive the NSR received",e);
                return false;
            }
            if(action.ordinal()==Action.INSTANTIATE_FINISH.ordinal())
                nsrSet.add(nsr);
            else if(action.ordinal()==Action.RELEASE_RESOURCES_FINISH.ordinal())
                nsrSet.remove(nsr);
            else {
                log.debug("Action unknow: "+action);
            }
            return true;
        }

        private String read(InputStream is) throws IOException {

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;

            try
            {
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                streamReader.close();
            }
            return responseStrBuilder.toString();
        }
    }

}
