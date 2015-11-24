package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.catalogue.mano.common.faultmanagement.AlarmState;

/**
 * Created by mob on 19.11.15.
 */
public interface AlarmRepositoryCustom  {
    Alarm changeAlarmState(String triggerId, AlarmState alarmState);
}
