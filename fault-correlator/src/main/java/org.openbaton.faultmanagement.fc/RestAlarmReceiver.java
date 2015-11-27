package org.openbaton.faultmanagement.fc;

import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.faultmanagement.fc.interfaces.AlarmReceiver;
import org.openbaton.faultmanagement.fc.repositories.AlarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by mob on 09.11.15.
 */
@RestController
@RequestMapping("/alarm")
public class RestAlarmReceiver implements AlarmReceiver {

    private static final Logger log = LoggerFactory.getLogger(RestAlarmReceiver.class);
    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    org.openbaton.faultmanagement.fc.interfaces.FaultCorrelatorManager faultCorrelatorManager;

    @Override
    @RequestMapping(value = "/vnf", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm receiveVnfNewAlarm(@RequestBody @Valid VNFAlarmNotification vnfAlarm) {
            Alarm alarm = alarmRepository.save(vnfAlarm.getAlarm());
            faultCorrelatorManager.newVnfAlarm(alarm);
        return alarm;
    }

    @Override
    @RequestMapping(value = "/vnf", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Alarm receiveVnfStateChangedAlarm(@RequestBody @Valid VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification) {
        if(vnfAlarmStateChangedNotification!=null && vnfAlarmStateChangedNotification.getAlarmState()!=null
                && ! vnfAlarmStateChangedNotification.getResourceId().isEmpty()){
            //TODO
        }
        return null;
    }

    @Override
    @RequestMapping(value = "/vr", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Alarm receiveVRNewAlarm(@RequestBody @Valid VirtualizedResourceAlarmNotification vrAlarm) {
        log.debug("received: "+vrAlarm);
        log.debug("Saving new vr alarm: "+vrAlarm.getAlarm());
        Alarm alarm = alarmRepository.save(vrAlarm.getAlarm());
        faultCorrelatorManager.newVRAlarm(alarm);
        return alarm;
    }

    @Override
    @RequestMapping(value = "/vr", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Alarm receiveVRStateChangedAlarm(@RequestBody @Valid VirtualizedResourceAlarmStateChangedNotification vrascn) {
        Alarm alarm = alarmRepository.changeAlarmState(vrascn.getTriggerId(), vrascn.getAlarmState());
        if(alarm!=null)
            log.debug("Changed alarm state to: " + alarm.getAlarmState());
        return alarm;
    }
}
