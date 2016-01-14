package org.openbaton.faultmanagement.events.register;

import org.openbaton.faultmanagement.events.SubscriptionRegister;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Created by mob on 28.10.15.
 */
/*@Component*/
public class JmsRegister extends SubscriptionRegister {

    /*@Override
    @JmsListener(destination = "vnf-alarm-unsubscribe", containerFactory = "queueJmsContainerFactory")
    public void unsubscribe(String id) throws NotFoundException {
        this.deleteSubscription(id);
    }
    @Override
    @JmsListener(destination = "vnf-alarm-subscribe", containerFactory = "queueJmsContainerFactory")
    public String subscribe(@Payload AlarmEndpoint endpoint) {
        AlarmEndpoint alarmEndpoint = this.saveSubscription(endpoint);
        return alarmEndpoint.getId();
    }
*/
}
