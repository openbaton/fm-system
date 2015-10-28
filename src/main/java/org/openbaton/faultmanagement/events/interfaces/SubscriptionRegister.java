package org.openbaton.faultmanagement.events.interfaces;

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.model.AlarmEndpoint;

/**
 * Created by mob on 28.10.15.
 */
public interface SubscriptionRegister {
    void unsubscribe(String id) throws NotFoundException;
    String subscribe(AlarmEndpoint endpoint);
}
