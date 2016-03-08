package org.openbaton.faultmanagement.fc;

import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFAlarmNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.faultmanagement.fc.interfaces.EventReceiver;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.fc.repositories.VNFAlarmRepository;
import org.openbaton.faultmanagement.fc.repositories.VRAlarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by mob on 09.11.15.
 */
@RestController
public class RestEventReceiver implements EventReceiver {

    private static final Logger log = LoggerFactory.getLogger(RestEventReceiver.class);

    @Autowired
    private PolicyManager policyManager;
    @Autowired
    private KieSession kieSession;
    @Autowired private VNFAlarmRepository vnfAlarmRepository;
    @Autowired private VRAlarmRepository vrAlarmRepository;

    @Override
    @RequestMapping(value = "/alarm/vnf", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm receiveVnfNewAlarm(@RequestBody @Valid VNFAlarmNotification vnfAlarm) {
        log.debug("Received new VNF alarm");
        kieSession.insert(vnfAlarm);
        return vnfAlarm.getAlarm();
    }

    @Override
    @RequestMapping(value = "/alarm/vnf", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Alarm receiveVnfStateChangedAlarm(@RequestBody @Valid VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification) {
        log.debug("Received VNF state changed Alarm");
        kieSession.insert(vnfAlarmStateChangedNotification);
        return vnfAlarmRepository.findFirstByThresholdId(vnfAlarmStateChangedNotification.getThresholdId());
    }

    @Override
    @RequestMapping(value = "/alarm/vr", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm receiveVRNewAlarm(@RequestBody @Valid VirtualizedResourceAlarmNotification vrAlarmNot) {
        log.debug("Received new VR alarm");
        kieSession.insert(vrAlarmNot.getVrAlarm());
        return vrAlarmNot.getVrAlarm();
    }

    @Override
    @RequestMapping(value = "/alarm/vr", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Alarm receiveVRStateChangedAlarm(@RequestBody @Valid VirtualizedResourceAlarmStateChangedNotification vrascn) {
        log.debug("Received VR state changed alarm");
        kieSession.insert(vrascn);
        return vrAlarmRepository.findFirstByThresholdId(vrascn.getTriggerId());
    }

    @Override
    @RequestMapping(value = "/nfvo/events", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void eventFromNfvo(@RequestBody @Valid OpenbatonEvent openbatonEvent) {
        log.debug("Received nfvo event with action: " + openbatonEvent.getAction());
        try {
            boolean isNSRManaged = policyManager.isNSRManaged(openbatonEvent.getPayload().getId());
            //Here we consider every instantiatie finish as recovery action finished
            if(openbatonEvent.getAction().ordinal() == Action.INSTANTIATE_FINISH.ordinal()){
                recoveryActionFinished();
            }
            if (openbatonEvent.getAction().ordinal() == Action.INSTANTIATE_FINISH.ordinal() && !isNSRManaged) {
                    policyManager.manageNSR(openbatonEvent.getPayload());
            } else if (openbatonEvent.getAction().ordinal() == Action.RELEASE_RESOURCES_FINISH.ordinal() && isNSRManaged) {
                    policyManager.unManageNSR(openbatonEvent.getPayload());
            }
        }catch (Exception e){
            log.error("Receiving the openbaton event: "+openbatonEvent+" "+e.getMessage(),e);
        }
    }

    private void recoveryActionFinished() {
        RecoveryAction recoveryAction = new RecoveryAction(RecoveryActionType.SWITCH_TO_STANDBY,"","");
        recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
        kieSession.insert(recoveryAction);
    }

}
