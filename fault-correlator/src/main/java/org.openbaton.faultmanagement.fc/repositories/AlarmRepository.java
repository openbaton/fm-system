package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.AlarmType;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by mob on 26.10.15.
 */
public interface AlarmRepository extends CrudRepository<Alarm, String> ,AlarmRepositoryCustom{
    List<Alarm> findByThresholdIdAndPerceivedSeverity(String thresholdId, PerceivedSeverity perceivedSeverity);
    List<Alarm> findByThresholdIdAndAlarmStateNot(String threshold, AlarmState alarmState);
    Alarm findFirstByThresholdId(String threshold);
    //List<Alarm> findByResourceIdAndAlarmStateNotAndAlarmType(String resourceId, AlarmState alarmState, AlarmType alarmType);
}
