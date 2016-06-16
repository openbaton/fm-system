/*
* Copyright (c) 2015-2016 Fraunhofer FOKUS
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.openbaton.faultmanagement.fc.policymanagement;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.catalogue.security.Project;
import org.openbaton.faultmanagement.fc.ConfigurationBeans;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.EventSubscriptionManger;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 13.05.16.
 */
@Service
@ConfigurationProperties
public class EventSubscriptionManagerImpl implements EventSubscriptionManger,CommandLineRunner,ApplicationListener<ContextClosedEvent> {

    private NFVORequestor nfvoRequestor;
    @Value("${nfvo-usr:}")
    private String nfvoUsr;
    @Value("${nfvo-pwd:}")
    private String nfvoPwd;
    @Value("${nfvo.ip:}")
    private String nfvoIp;
    @Value("${nfvo.port:8080}")
    private String nfvoPort;
    @Value("${server.port:}")
    private String fmsPort;
    private String projectId;

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private String unsubscriptionIdINSTANTIATE_FINISH;
    private String unsubscriptionIdRELEASE_RESOURCES_FINISH;
    private List<String> unsubscriptionIdList;

    @PostConstruct
    public void init(){

        this.nfvoRequestor = new NFVORequestor(nfvoUsr,nfvoPwd, null,false,nfvoIp,nfvoPort,"1");
        try {
            for (Project project : nfvoRequestor.getProjectAgent().findAll()) {
                if (project.getName().equals("default")) {
                    projectId = project.getId();
                }
            }
        } catch (ClassNotFoundException|SDKException e) {
            e.printStackTrace();
        }

        unsubscriptionIdList=new ArrayList<>();
    }

    @Override
    public String subscribe(NetworkServiceRecord networkServiceRecord, Action action) throws SDKException {
        EventEndpoint eventEndpoint = createEventEndpoint("FM-nsr-"+action,EndpointType.RABBIT,action, ConfigurationBeans.queueName_vnfEvents);
        eventEndpoint.setVirtualNetworkFunctionId(networkServiceRecord.getId());
        EventEndpoint response = sendSubscription(eventEndpoint);
        unsubscriptionIdList.add(response.getId());
        return response.getId();
    }


    @Override
    public String subscribe(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Action action) throws SDKException {
        EventEndpoint eventEndpoint = createEventEndpoint("FM-vnfr-"+action,EndpointType.RABBIT,action, ConfigurationBeans.queueName_vnfEvents);
        eventEndpoint.setVirtualNetworkFunctionId(virtualNetworkFunctionRecord.getId());
        EventEndpoint response=sendSubscription(eventEndpoint);
        unsubscriptionIdList.add(response.getId());
        return response.getId();
    }

    private EventEndpoint sendSubscription(EventEndpoint eventEndpoint) throws SDKException {
        nfvoRequestor.setProjectId(projectId);
        return nfvoRequestor.getEventAgent().create(eventEndpoint);
    }

    @Override
    public void unSubscribe(String subscriptionId) throws SDKException {
        nfvoRequestor.setProjectId(projectId);
        nfvoRequestor.getEventAgent().requestDelete(subscriptionId);
    }

    private EventEndpoint createEventEndpoint(String name, EndpointType type, Action action, String url){
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(action);
        eventEndpoint.setName(name);
        eventEndpoint.setType(type);
        eventEndpoint.setEndpoint(url);
        return eventEndpoint;
    }

    @Override
    public void run(String... args) throws Exception {


        if(nfvoIp==null || nfvoIp.isEmpty())
            throw new NullPointerException("The nfvoIp is not present. Please set the 'nfvo.ip' property in the fms.properties");
        log.info("NFVO ip: "+nfvoIp);
        log.info("NFVO port: "+nfvoPort);
        log.info("FMS port: "+ fmsPort);

        EventEndpoint eventEndpointInstantiateFinish = createEventEndpoint("FM-nsr-INSTANTIATE_FINISH", EndpointType.RABBIT, Action.INSTANTIATE_FINISH, ConfigurationBeans.queueName_eventInstatiateFinish);
        //TODO Subscribe for REALEASE_RESOURCES_FINISH only for nsr which require fault management
        EventEndpoint eventEndpointReleaseResourcesFinish = createEventEndpoint("FM-nsr-RELEASE_RESOURCES_FINISH",EndpointType.RABBIT,Action.RELEASE_RESOURCES_FINISH,ConfigurationBeans.queueName_eventResourcesReleaseFinish);

        EventEndpoint response=sendSubscription(eventEndpointInstantiateFinish);
        unsubscriptionIdINSTANTIATE_FINISH = response.getId();
        response=sendSubscription(eventEndpointReleaseResourcesFinish);
        unsubscriptionIdRELEASE_RESOURCES_FINISH = response.getId();
        log.info("Correctly registered to the NFVO");
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try{
            unSubscribe(unsubscriptionIdINSTANTIATE_FINISH);
            unSubscribe(unsubscriptionIdRELEASE_RESOURCES_FINISH);

            log.debug("unsubscribing vnf event subscriptions");
            for(String unsubscriptionId : unsubscriptionIdList){
                unSubscribe(unsubscriptionId);
            }

        } catch (SDKException e) {
            log.error("The NFVO is not available for unsubscriptions: "+e.getMessage(),e);
        }
    }
}
