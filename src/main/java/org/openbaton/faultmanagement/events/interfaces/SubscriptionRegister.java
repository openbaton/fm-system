package org.openbaton.faultmanagement.events.interfaces;

import org.openbaton.catalogue.mano.common.monitoring.AlarmEndpoint;

/**
 * Created by mob on 28.10.15.
 */
public interface SubscriptionRegister {
    //void unsubscribe(String id) throws NotFoundException;
    String subscribe(AlarmEndpoint endpoint);
}
