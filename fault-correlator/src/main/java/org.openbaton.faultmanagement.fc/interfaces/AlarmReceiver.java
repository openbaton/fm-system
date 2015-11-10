package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.faultmanagement.events.notifications.AbstractVNFAlarm;

/**
 * Created by mob on 09.11.15.
 */
public interface AlarmReceiver {
    void receiveVnfAlarm(AbstractVNFAlarm abstractVNFAlarm);
}
