package org.openbaton.faultmanagement.repo;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;

/**
 * Created by mob on 31.01.16.
 */
public interface VRAlarmRepositoryCustom {
  VRAlarm changeAlarmState(String thresholdId, AlarmState alarmState);
}
