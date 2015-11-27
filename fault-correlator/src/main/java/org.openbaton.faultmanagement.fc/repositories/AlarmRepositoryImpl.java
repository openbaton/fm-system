package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by mob on 19.11.15.
 */
@Transactional(readOnly = true)
public class AlarmRepositoryImpl implements AlarmRepositoryCustom{
    @Autowired
    private AlarmRepository alarmRepository;

    @Override
    @Transactional
    public Alarm changeAlarmState(String triggerId, AlarmState alarmState) {
        Alarm alarm=alarmRepository.findFirstByTriggerId(triggerId);
        if(alarm!=null) {
            alarm.setAlarmState(alarmState);
        }
        return alarm;
    }
}
