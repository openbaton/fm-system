package org.openbaton.faultmanagement.events;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.catalogue.mano.common.faultmanagement.AlarmState;

/**
 * Created by mob on 28.10.15.
 */
public class VNFAlarmStateChangedNotification extends AbstractVNFAlarm {
    private String alarmId;
    private AlarmState alarmState;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public VNFAlarmStateChangedNotification(Object source, Alarm alarm,String alarmId, AlarmState alarmState) {
        super(source,alarm);
        this.alarmId=alarmId;
        this.alarmState=alarmState;

    }

    public String getAlarmId() {
        return alarmId;
    }

    public AlarmState getAlarmState() {
        return alarmState;
    }

}
