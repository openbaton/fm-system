package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.springframework.http.ResponseEntity;

/**
 * Created by mob on 09.11.15.
 */
public interface AlarmReceiver {
    Alarm receiveVnfNewAlarm(VNFAlarmNotification vnfAlarm);
    Alarm receiveVnfStateChangedAlarm(VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification);

    Alarm receiveVRNewAlarm(VirtualizedResourceAlarmNotification vrAlarm);
    Alarm receiveVRStateChangedAlarm(VirtualizedResourceAlarmStateChangedNotification abstractVNFAlarm);
}
