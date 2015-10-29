package org.openbaton.faultmanagement.events.notifications;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;

/**
 * Created by mob on 27.10.15.
 */
public class VNFAlarmNotification extends AbstractVNFAlarm {
    private Alarm alarm;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public VNFAlarmNotification(Object source, Alarm alarm) {
        super(source,alarm);
    }
    public Alarm getAlarm() {
        return alarm;
    }
}
