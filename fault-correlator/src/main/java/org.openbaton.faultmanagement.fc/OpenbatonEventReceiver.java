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

package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by mob on 23.03.16.
 */
@Service
public class OpenbatonEventReceiver {
    private static final Logger logger = LoggerFactory.getLogger(OpenbatonEventReceiver.class);

    @Autowired private Gson mapper;
    @Autowired private PolicyManager policyManager;
    @Autowired private KieSession kieSession;


    public void receiveNewNsr(String message) {
        OpenbatonEvent openbatonEvent;
        try {
            openbatonEvent = getOpenbatonEvent(message);
            logger.debug("Received nfvo event with action: " + openbatonEvent.getAction());

            NetworkServiceRecord nsr = getNsrFromPayload(openbatonEvent.getPayload());
            boolean isNSRManaged = policyManager.isNSRManaged(nsr.getId());
            //Here we consider every instantiate finish as recovery action finished
            recoveryActionFinished();

            if (!isNSRManaged)
                policyManager.manageNSR(nsr);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return;
        }
    }

    public void vnfEvent(String message){
        OpenbatonEvent openbatonEvent;
        try {
            openbatonEvent = getOpenbatonEvent(message);

        logger.debug("Received VNF event with action: " + openbatonEvent.getAction());
        VirtualNetworkFunctionRecord vnfr = getVnfrFromPayload(openbatonEvent.getPayload());
        if (openbatonEvent.getAction().ordinal() == Action.HEAL.ordinal()){
            RecoveryAction recoveryAction = new RecoveryAction();
            recoveryAction.setVnfrId(vnfr.getId());
            recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
            kieSession.insert(recoveryAction);
        }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return;
        }
    }

    public void deleteNsr(String message){
        OpenbatonEvent openbatonEvent;
        try {
            openbatonEvent = getOpenbatonEvent(message);

        logger.debug("Received nfvo event with action: " + openbatonEvent.getAction());
        NetworkServiceRecord nsr = getNsrFromPayload(openbatonEvent.getPayload());

            boolean isNSRManaged = policyManager.isNSRManaged(nsr.getId());
            if (isNSRManaged)
                policyManager.unManageNSR(nsr);
        } catch (Exception e) {
            logger.warn(e.getMessage(),e);
            return;
        }
    }

    private OpenbatonEvent getOpenbatonEvent(String message) throws NFVORequestorException {
        OpenbatonEvent openbatonEvent;

        try {
            openbatonEvent = mapper.fromJson(message, OpenbatonEvent.class);
        } catch (JsonParseException e) {
            throw new NFVORequestorException(e.getMessage(),e);
        }
        return openbatonEvent;
    }

    private NetworkServiceRecord getNsrFromPayload(JsonObject nsrJson) throws NFVORequestorException {
        NetworkServiceRecord networkServiceRecord;
        try {
            networkServiceRecord = mapper.fromJson(nsrJson, NetworkServiceRecord.class);
        } catch (JsonParseException e) {
            throw new NFVORequestorException(e.getMessage(),e);
        }
        return networkServiceRecord;
    }
    private VirtualNetworkFunctionRecord getVnfrFromPayload(JsonObject vnfrJson) throws NFVORequestorException {
        VirtualNetworkFunctionRecord vnfr;
        try {
            vnfr = mapper.fromJson(vnfrJson, VirtualNetworkFunctionRecord.class);
        } catch (JsonParseException e) {
            throw new NFVORequestorException(e.getMessage(),e);
        }
        return vnfr;
    }
    private void recoveryActionFinished() {
        RecoveryAction recoveryAction = new RecoveryAction();
        recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
        kieSession.insert(recoveryAction);
    }
}
