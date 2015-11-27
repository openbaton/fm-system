package org.openbaton.faultmanagement.fc;

import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.faultmanagement.fc.repositories.AlarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mob on 09.11.15.
 */
@Service
public class FaultCorrelatorManager implements org.openbaton.faultmanagement.fc.interfaces.FaultCorrelatorManager{

    private Map<String, List<Alarm>> alarmRepo;
    private static final Logger log = LoggerFactory.getLogger(FaultCorrelatorManager.class);
    @Autowired
    AlarmRepository alarmRepository;

    @PostConstruct
    public void init(){
        alarmRepo=new HashMap<>();
        alarmRepo.put("VNF Alarms", new ArrayList<Alarm>());
        alarmRepo.put("VIM Alarms", new ArrayList<Alarm>());
    }

    @Override
    public void newVnfAlarm(Alarm vnfAlarm) {
        log.debug("New VNF alarm: "+vnfAlarm);

        if(vnfAlarm.getPerceivedSeverity().ordinal()== PerceivedSeverity.CRITICAL.ordinal()){
            //check if there are alarms in the same vm
            // get vnfr
            //List<Alarm> activeAlarms = alarmRepository.findByResourceIdAndAlarmStateNot("hostname",AlarmState.CLEARED);
            /*if(activeAlarms.isEmpty()){
                //get VNFFaultManagementPolicy
                //executeActionInVNFPolicy(VNFFaultManagementPolicy);
            }*/
        }
    }

    @Override
    public void newVRAlarm(Alarm vrAlarm) {
        log.debug("New VR alarm: "+vrAlarm);



    }

    @Override
    public void updateStatusVnfAlarm(VNFAlarmStateChangedNotification vnfascn) {

    }

    @Override
    public void updateStatusVRAlarm(VirtualizedResourceAlarmStateChangedNotification vrascn) {

    }
}
