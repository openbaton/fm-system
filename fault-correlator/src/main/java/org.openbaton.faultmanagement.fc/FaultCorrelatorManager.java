package org.openbaton.faultmanagement.fc;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.faultmanagement.events.notifications.AbstractVNFAlarm;
import org.openbaton.faultmanagement.events.notifications.VNFAlarmNotification;
import org.openbaton.faultmanagement.events.notifications.VNFAlarmStateChangedNotification;
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
public class FaultCorrelatorManager {

    private Map<String, List<Alarm>> alarmRepo;

    @PostConstruct
    public void init(){
        alarmRepo=new HashMap<>();
        alarmRepo.put("VNF Alarms", new ArrayList<Alarm>());
        alarmRepo.put("VIM Alarms", new ArrayList<Alarm>());
    }

    public void manageAlarm(AbstractVNFAlarm abstractVNFAlarm){
        if(abstractVNFAlarm==null)
            throw new NullPointerException("The abstractVNFAlarm is null");
        Alarm alarm=null;
        if(abstractVNFAlarm instanceof VNFAlarmNotification){
            VNFAlarmNotification vnfAlarmNotification=(VNFAlarmNotification) abstractVNFAlarm;
            alarm = saveVnfAlarmNotification(vnfAlarmNotification);
        }
        else if(abstractVNFAlarm instanceof VNFAlarmStateChangedNotification){
            VNFAlarmStateChangedNotification vnfascn=(VNFAlarmStateChangedNotification) abstractVNFAlarm;
            alarm = saveVnfAlarmStateChangedNotification(vnfascn);
        }
    }

    private Alarm saveVnfAlarmNotification(VNFAlarmNotification vnfAlarmNotification) {
        Alarm alarm = vnfAlarmNotification.getAlarm();
        List<Alarm> updatedList = alarmRepo.get("VNF Alarms");
        updatedList.add(alarm);
        alarmRepo.put("VNF Alarms",updatedList);
        return alarm;
    }

    private Alarm saveVnfAlarmStateChangedNotification(VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification) {
        Alarm alarm = vnfAlarmStateChangedNotification.getAlarm();
        List<Alarm> alarms = alarmRepo.get("VNF Alarms");
        /*
        updatedList.add(alarm);
        alarmRepo.put("VNF Alarm",updatedList);*/
        return alarm;
    }

}
