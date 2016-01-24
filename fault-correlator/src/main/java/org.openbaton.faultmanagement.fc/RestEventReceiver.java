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
import org.openbaton.faultmanagement.fc.repositories.AlarmRepository;
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
    AlarmRepository alarmRepository;
    @Autowired
    PolicyManager policyManager;
    @Autowired
    private KieSession kieSession;
    @Autowired
    org.openbaton.faultmanagement.fc.interfaces.FaultCorrelatorManager faultCorrelatorManager;

    @Override
    @RequestMapping(value = "/alarm/vnf", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm receiveVnfNewAlarm(@RequestBody @Valid VNFAlarmNotification vnfAlarm) {
            Alarm alarm = alarmRepository.save(vnfAlarm.getAlarm());
            faultCorrelatorManager.newVnfAlarm(alarm);
        return alarm;
    }

    @Override
    @RequestMapping(value = "/alarm/vnf", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Alarm receiveVnfStateChangedAlarm(@RequestBody @Valid VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification) {
        if(vnfAlarmStateChangedNotification!=null && vnfAlarmStateChangedNotification.getAlarmState()!=null
                && ! vnfAlarmStateChangedNotification.getResourceId().isEmpty()){
            //TODO
        }
        return null;
    }

    @Override
    @RequestMapping(value = "/alarm/vr", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm receiveVRNewAlarm(@RequestBody @Valid VirtualizedResourceAlarmNotification vrAlarm) {
        log.debug("Received  new VR alarm");
        kieSession.insert(vrAlarm.getAlarm());
        kieSession.fireAllRules();

        return vrAlarm.getAlarm();
    }

    @Override
    @RequestMapping(value = "/alarm/vr", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Alarm receiveVRStateChangedAlarm(@RequestBody @Valid VirtualizedResourceAlarmStateChangedNotification vrascn) {
        Alarm alarm = alarmRepository.changeAlarmState(vrascn.getTriggerId(), vrascn.getAlarmState());
        if(policyManager.isAManagedAlarm(vrascn.getTriggerId())){
            String vnfrId = policyManager.getVNFRShort(vrascn.getTriggerId()).getId();
            VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification = new VNFAlarmStateChangedNotification(vnfrId,vrascn.getTriggerId(),vrascn.getAlarmState());
            faultCorrelatorManager.updateStatusVnfAlarm(vnfAlarmStateChangedNotification);
        }
        else
            faultCorrelatorManager.updateStatusVRAlarm(vrascn);
        return alarm;
    }

    @Override
    @RequestMapping(value = "/nfvo/events", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void eventFromNfvo(@RequestBody @Valid OpenbatonEvent openbatonEvent) {
        log.info("Received nfvo event with action: " + openbatonEvent.getAction());
        try {
            boolean isNSRManaged = policyManager.isNSRManaged(openbatonEvent.getPayload().getId());
            if (openbatonEvent.getAction().ordinal() == Action.INSTANTIATE_FINISH.ordinal() && !isNSRManaged) {
                    policyManager.manageNSR(openbatonEvent.getPayload());
            } else if (openbatonEvent.getAction().ordinal() == Action.RELEASE_RESOURCES_FINISH.ordinal() && isNSRManaged) {
                    policyManager.unManageNSR(openbatonEvent.getPayload());
            }
        }catch (Exception e){
            log.error("Receiving the openbaton event: "+openbatonEvent+" "+e.getMessage(),e);
        }
    }

}
