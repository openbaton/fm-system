package org.openbaton.faultmanagement.events.interfaces;

import org.openbaton.faultmanagement.events.AbstractVNFAlarm;

/**
 * Created by mob on 27.10.15.
 */
public interface AlarmDispatcher {
    void dispatchAlarm(AbstractVNFAlarm event);
}
