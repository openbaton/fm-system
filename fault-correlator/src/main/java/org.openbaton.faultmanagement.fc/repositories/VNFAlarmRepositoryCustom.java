package org.openbaton.faultmanagement.fc.repositories;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;

/**
 * Created by mob on 31.01.16.
 */
public interface VNFAlarmRepositoryCustom {
    VNFAlarm changeAlarmState(String vnfrId, AlarmState alarmState);
}
