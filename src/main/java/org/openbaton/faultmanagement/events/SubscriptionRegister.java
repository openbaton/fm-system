package org.openbaton.faultmanagement.events;

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.model.AlarmEndpoint;
import org.openbaton.faultmanagement.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by mob on 28.10.15.
 */
@Service
public class SubscriptionRegister implements org.openbaton.faultmanagement.events.interfaces.SubscriptionRegister {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public void unsubscribe(String id) throws NotFoundException {
        throw new UnsupportedOperationException();
    }

    public String subscribe(AlarmEndpoint endpoint) {
        throw new UnsupportedOperationException();
    }

    protected AlarmEndpoint saveSubscription(AlarmEndpoint alarmEndpoint){
        return subscriptionRepository.save(alarmEndpoint);
    }

    protected void deleteSubscription(String subscriptionId) throws NotFoundException {
        if(subscriptionId==null || subscriptionId.isEmpty())
            throw new NullPointerException("The subscription id is null or empty");
        if(!subscriptionRepository.exists(subscriptionId))
            throw new NotFoundException("The subscription id not exist");
        subscriptionRepository.delete(subscriptionId);
    }

    public SubscriptionRepository getSubscriptionRepository() {
        return subscriptionRepository;
    }
}
