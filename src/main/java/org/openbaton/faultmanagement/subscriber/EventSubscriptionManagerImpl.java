/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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

package org.openbaton.faultmanagement.subscriber;

import java.io.FileNotFoundException;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.faultmanagement.catalogue.ManagedNetworkServiceRecord;
import org.openbaton.faultmanagement.core.ham.exceptions.HighAvailabilityException;
import org.openbaton.faultmanagement.core.pm.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.receivers.RabbitEventReceiverConfiguration;
import org.openbaton.faultmanagement.repo.ManagedNetworkServiceRecordRepository;
import org.openbaton.faultmanagement.requestor.interfaces.NFVORequestorWrapper;
import org.openbaton.faultmanagement.subscriber.interfaces.EventSubscriptionManger;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/** Created by mob on 13.05.16. */
@Service
@ConfigurationProperties
public class EventSubscriptionManagerImpl implements EventSubscriptionManger {

  @Autowired private NFVORequestorWrapper nfvoRequestor;
  @Autowired private ManagedNetworkServiceRecordRepository mnsrRepo;
  private Logger log = LoggerFactory.getLogger(this.getClass());
  private String unsubscriptionIdINSTANTIATE_FINISH;
  private String unsubscriptionIdRELEASE_RESOURCES_FINISH;

  @Override
  public String subscribe(NetworkServiceRecord networkServiceRecord, Action action)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    EventEndpoint eventEndpoint =
        createEventEndpoint(
            "FM-nsr-" + action,
            EndpointType.RABBIT,
            action,
            RabbitEventReceiverConfiguration.queueName_eventInstatiateFinish);
    eventEndpoint.setNetworkServiceId(networkServiceRecord.getId());
    String id = sendSubscription(networkServiceRecord.getProjectId(), eventEndpoint);
    mnsrRepo.addUnsubscriptionId(networkServiceRecord.getId(), id);
    return id;
  }

  @Override
  public String subscribe(VirtualNetworkFunctionRecord virtualNetworkFunctionRecord, Action action)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    EventEndpoint eventEndpoint =
        createEventEndpoint(
            "FM-vnfr-" + action,
            EndpointType.RABBIT,
            action,
            RabbitEventReceiverConfiguration.queueName_vnfEvents);
    eventEndpoint.setVirtualNetworkFunctionId(virtualNetworkFunctionRecord.getId());
    String id = sendSubscription(virtualNetworkFunctionRecord.getProjectId(), eventEndpoint);
    mnsrRepo.addUnsubscriptionId(virtualNetworkFunctionRecord.getParent_ns_id(), id);
    return id;
  }

  private String sendSubscription(String projectId, EventEndpoint eventEndpoint)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    return nfvoRequestor.subscribe(projectId, eventEndpoint);
  }

  private String sendSubscription(EventEndpoint eventEndpoint)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    return nfvoRequestor.subscribe(eventEndpoint);
  }

  @Override
  public void unSubscribe(String id)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    nfvoRequestor.unSubscribe(id);
  }

  private EventEndpoint createEventEndpoint(
      String name, EndpointType type, Action action, String url) {
    EventEndpoint eventEndpoint = new EventEndpoint();
    eventEndpoint.setEvent(action);
    eventEndpoint.setName(name);
    eventEndpoint.setType(type);
    eventEndpoint.setEndpoint(url);
    return eventEndpoint;
  }

  public void subscribeToNFVO()
      throws ClassNotFoundException, SDKException, HighAvailabilityException,
          FaultManagementPolicyException, FileNotFoundException {
    EventEndpoint eventEndpointInstantiateFinish =
        createEventEndpoint(
            "FM-nsr-INSTANTIATE_FINISH",
            EndpointType.RABBIT,
            Action.INSTANTIATE_FINISH,
            RabbitEventReceiverConfiguration.queueName_eventInstatiateFinish);
    //TODO Subscribe for RELEASE_RESOURCES_FINISH only for nsr which require fault management
    EventEndpoint eventEndpointReleaseResourcesFinish =
        createEventEndpoint(
            "FM-nsr-RELEASE_RESOURCES_FINISH",
            EndpointType.RABBIT,
            Action.RELEASE_RESOURCES_FINISH,
            RabbitEventReceiverConfiguration.queueName_eventResourcesReleaseFinish);
    unsubscriptionIdINSTANTIATE_FINISH = sendSubscription(eventEndpointInstantiateFinish);
    unsubscriptionIdRELEASE_RESOURCES_FINISH =
        sendSubscription(eventEndpointReleaseResourcesFinish);
    log.info("Correctly registered to the NFVO");
    log.debug("unsubscriptionIdINSTANTIATE_FINISH:" + unsubscriptionIdINSTANTIATE_FINISH);
    log.debug(
        "unsubscriptionIdRELEASE_RESOURCES_FINISH:" + unsubscriptionIdRELEASE_RESOURCES_FINISH);
  }

  public void unSubscribeToNFVO() {
    try {
      if (unsubscriptionIdINSTANTIATE_FINISH != null)
        unSubscribe(unsubscriptionIdINSTANTIATE_FINISH);
      if (unsubscriptionIdRELEASE_RESOURCES_FINISH != null)
        unSubscribe(unsubscriptionIdRELEASE_RESOURCES_FINISH);
      for (ManagedNetworkServiceRecord mnsr : mnsrRepo.findAll()) {
        for (String unsubscriptionId : mnsr.getUnSubscriptionIds()) {
          unSubscribe(unsubscriptionId);
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("The NFVO is not available for unsubscriptions: " + e.getMessage(), e);
      else log.error("The NFVO is not available for unsubscriptions: " + e.getMessage());
    }
  }
}
