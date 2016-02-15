package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by mob on 31.01.16.
 */
public class VNFAlarmRepositoryImpl implements VNFAlarmRepositoryCustom{

    @Autowired VNFAlarmRepository vnfAlarmRepository;

    @Override
    public VNFAlarm changeAlarmState(String thresholdId, AlarmState alarmState) {
        VNFAlarm vnfAlarm = vnfAlarmRepository.findFirstByThresholdId(thresholdId);
        if(vnfAlarm!=null){
            vnfAlarm.setAlarmState(alarmState);
        }
        return vnfAlarm;
    }
}
