package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mob on 26.10.15.
 */
public interface VNFAlarmRepository extends CrudRepository<VNFAlarm, String>,VNFAlarmRepositoryCustom{
    //List<Alarm> findByThresholdIdAndPerceivedSeverity(String thresholdId, PerceivedSeverity perceivedSeverity);
    //List<Alarm> findByThresholdIdAndAlarmStateNot(String threshold, AlarmState alarmState);
    VNFAlarm findFirstByThresholdId(String threshold);
    VNFAlarm findFirstByVnfrId(String vnfrId);
    //List<Alarm> findByResourceIdAndAlarmStateNotAndAlarmType(String resourceId, AlarmState alarmState, AlarmType alarmType);
}
