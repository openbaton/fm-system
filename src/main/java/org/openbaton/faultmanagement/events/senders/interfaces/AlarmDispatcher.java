package org.openbaton.faultmanagement.events.senders.interfaces;

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.events.AbstractVNFAlarm;
import org.openbaton.faultmanagement.model.AlarmEndpoint;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * Created by mob on 27.10.15.
 */
public interface AlarmDispatcher {
    AlarmEndpoint register(@Payload AlarmEndpoint endpoint);
    void dispatchEvent(AbstractVNFAlarm event);
    void unregister(String id) throws NotFoundException;
}
