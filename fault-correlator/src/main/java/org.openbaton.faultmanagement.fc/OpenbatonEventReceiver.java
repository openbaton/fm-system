package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.kie.api.runtime.KieSession;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
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
        } catch (NFVORequestorException e) {
            logger.warn(e.getMessage());
            return;
        }

        logger.debug("Received nfvo event with action: " + openbatonEvent.getAction());

        boolean isNSRManaged = policyManager.isNSRManaged(openbatonEvent.getPayload().getId());
        //Here we consider every instantiate finish as recovery action finished
        recoveryActionFinished();

        if (!isNSRManaged)
            policyManager.manageNSR(openbatonEvent.getPayload());
    }

    public void deleteNsr(String message){
        OpenbatonEvent openbatonEvent;
        try {
            openbatonEvent = getOpenbatonEvent(message);
        } catch (NFVORequestorException e) {
            logger.warn(e.getMessage());
            return;
        }
        logger.debug("Received nfvo event with action: " + openbatonEvent.getAction());
        try {
            boolean isNSRManaged = policyManager.isNSRManaged(openbatonEvent.getPayload().getId());
            if (isNSRManaged)
                policyManager.unManageNSR(openbatonEvent.getPayload());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
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

    private void recoveryActionFinished() {
        RecoveryAction recoveryAction = new RecoveryAction();
        recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
        kieSession.insert(recoveryAction);
    }
}
