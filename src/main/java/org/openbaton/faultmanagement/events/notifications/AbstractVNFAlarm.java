package org.openbaton.faultmanagement.events.notifications;
import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.springframework.context.ApplicationEvent;

/**
 * Created by mob on 27.10.15.
 */
public abstract class AbstractVNFAlarm extends ApplicationEvent {
    private Alarm alarm;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public AbstractVNFAlarm(Object source,Alarm alarm) {
        super(source);
        this.alarm=alarm;
    }

    public Alarm getAlarm() {
        return alarm;
    }

}
