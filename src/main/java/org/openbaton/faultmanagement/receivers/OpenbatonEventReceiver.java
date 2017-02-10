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

package org.openbaton.faultmanagement.receivers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.faultmanagement.catalogue.RecoveryAction;
import org.openbaton.faultmanagement.catalogue.RecoveryActionStatus;
import org.openbaton.faultmanagement.core.ham.interfaces.HighAvailabilityManager;
import org.openbaton.faultmanagement.core.pm.interfaces.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by mob on 23.03.16. */
@Service
public class OpenbatonEventReceiver {
  private static final Logger logger = LoggerFactory.getLogger(OpenbatonEventReceiver.class);

  @Autowired private Gson mapper;
  @Autowired private PolicyManager policyManager;
  @Autowired private KieSession kieSession;
  @Autowired private HighAvailabilityManager ham;
  private String failedVnfrId = "";

  public void receiveNewNsr(String message) {
    OpenbatonEvent openbatonEvent;
    try {
      openbatonEvent = getOpenbatonEvent(message);
      logger.debug("Received nfvo event with action: " + openbatonEvent.getAction());

      NetworkServiceRecord nsr = getNsrFromPayload(openbatonEvent.getPayload());
      /*// clean failed vnfc instances
      if (openbatonEvent.getAction() == Action.INSTANTIATE_FINISH)
        if (!failedVnfrId.isEmpty() && ham.hasFailedVnfcInstances(failedVnfrId))
          recoveryActionFinishedOnVnfr(failedVnfrId);
        else if (!failedVnfrId.isEmpty()) failedVnfrId = "";
          // clean check complete*/
      boolean isNSRManaged = policyManager.isNSRManaged(nsr.getId());
      if (isNSRManaged) {
        if (openbatonEvent.getAction() == Action.HEAL) {
          logger.debug("Recoveri action finished on nsr:" + nsr.getId());
          recoveryActionFinishedOnNsr(nsr.getId());
        }
      } else policyManager.manageNSR(nsr);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) logger.error(e.getMessage(), e);
      else logger.error(e.getMessage());
    }
  }

  public void vnfEvent(String message) {
    OpenbatonEvent openbatonEvent;
    try {
      openbatonEvent = getOpenbatonEvent(message);
      logger.debug("Received VNF event with action: " + openbatonEvent.getAction());
      //VirtualNetworkFunctionRecord vnfr = getVnfrFromPayload(openbatonEvent.getPayload());
    } catch (Exception e) {
      if (logger.isDebugEnabled()) logger.error(e.getMessage(), e);
      else logger.error(e.getMessage());
    }
  }

  public void deleteNsr(String message) {
    OpenbatonEvent openbatonEvent;
    try {
      openbatonEvent = getOpenbatonEvent(message);
      logger.debug("Received nfvo event with action: " + openbatonEvent.getAction());
      NetworkServiceRecord nsr = getNsrFromPayload(openbatonEvent.getPayload());
      if (policyManager.isNSRManaged(nsr.getId())) policyManager.unManageNSR(nsr);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) logger.error(e.getMessage(), e);
      else logger.error(e.getMessage());
    }
  }

  private OpenbatonEvent getOpenbatonEvent(String message) {
    return mapper.fromJson(message, OpenbatonEvent.class);
  }

  private NetworkServiceRecord getNsrFromPayload(JsonObject nsrJson) {
    return mapper.fromJson(nsrJson, NetworkServiceRecord.class);
  }

  private VirtualNetworkFunctionRecord getVnfrFromPayload(JsonObject vnfrJson) {
    return mapper.fromJson(vnfrJson, VirtualNetworkFunctionRecord.class);
  }

  private void recoveryActionFinishedOnVnfr(String vnfrId) {
    RecoveryAction recoveryAction = new RecoveryAction();
    recoveryAction.setVnfrId(vnfrId);
    recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
    kieSession.insert(recoveryAction);
  }

  private void recoveryActionFinishedOnNsr(String nsrId) {
    RecoveryAction recoveryAction = new RecoveryAction();
    recoveryAction.setNsrId(nsrId);
    recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
    kieSession.insert(recoveryAction);
  }
}
