package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;

/**
 * Created by mob on 09.11.15.
 */
public interface AlarmReceiver {
    void receiveVnfAlarm(Alarm alarm);
}
