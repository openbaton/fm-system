package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.AlarmType;

import java.util.List;

/**
 * Created by mob on 19.11.15.
 */
public interface AlarmRepositoryCustom  {
    Alarm changeAlarmState(String triggerId, AlarmState alarmState);
}
