package org.openbaton.faultmanagement.repo;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by mob on 31.01.16.
 */
@Transactional(readOnly = true)
public class VRAlarmRepositoryImpl implements VRAlarmRepositoryCustom {

    @Autowired private VRAlarmRepository vrAlarmRepository;

    @Override
    @Transactional
    public VRAlarm changeAlarmState(String thresholdId, AlarmState alarmState) {
        VRAlarm vrAlarm=vrAlarmRepository.findFirstByThresholdId(thresholdId);
        if(vrAlarm!=null) {
            vrAlarm.setAlarmState(alarmState);
        }
        return vrAlarm;
    }
}
