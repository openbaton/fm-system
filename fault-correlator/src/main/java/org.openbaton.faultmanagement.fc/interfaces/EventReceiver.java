package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.VNFAlarmNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.faultmanagement.fc.OpenbatonEvent;

/**
 * Created by mob on 09.11.15.
 */
public interface EventReceiver {
    Alarm receiveVnfNewAlarm(VNFAlarmNotification vnfAlarm);
    Alarm receiveVnfStateChangedAlarm(VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification);

    Alarm receiveVRNewAlarm(VirtualizedResourceAlarmNotification vrAlarm);
    Alarm receiveVRStateChangedAlarm(VirtualizedResourceAlarmStateChangedNotification abstractVNFAlarm);
    void eventFromNfvo(OpenbatonEvent openbatonEvent);
}
