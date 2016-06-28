package org.openbaton.faultmanagement.repo;


import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by mob on 31.01.16.
 */
public interface VRAlarmRepository extends CrudRepository<VRAlarm, String>,VRAlarmRepositoryCustom{
    List<VRAlarm> findByManagedObject(String managedObject);
    List<VRAlarm> findByManagedObjectAndAlarmStateNot(String managedObject, AlarmState alarmState);
    VRAlarm findFirstByThresholdId(String thresholdId);
    @Transactional
    List<VRAlarm> removeByManagedObject(String managedObject);
}
