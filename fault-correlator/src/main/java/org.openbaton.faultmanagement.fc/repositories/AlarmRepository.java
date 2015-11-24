package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.catalogue.mano.common.faultmanagement.AlarmState;
import org.openbaton.catalogue.mano.common.faultmanagement.PerceivedSeverity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by mob on 26.10.15.
 */
public interface AlarmRepository extends CrudRepository<Alarm, String> ,AlarmRepositoryCustom{
    List<Alarm> findByTriggerIdAndPerceivedSeverity(String triggerId, PerceivedSeverity perceivedSeverity);
    List<Alarm> findByTriggerIdAndAlarmStateNot(String triggerId, AlarmState alarmState);
    List<Alarm> findByResourceId(String resourceId);
    Alarm findFirstByTriggerId(String triggerId);
}
