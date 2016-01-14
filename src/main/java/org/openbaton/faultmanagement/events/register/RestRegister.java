package org.openbaton.faultmanagement.events.register;

import org.openbaton.faultmanagement.events.SubscriptionRegister;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mob on 28.10.15.
 */
/*@RestController
@RequestMapping("/admin/v1")*/
public class RestRegister extends SubscriptionRegister {

    /*@Override
    @RequestMapping(value = "/vnf-alarm-unsubscribe", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void unsubscribe(String id) throws NotFoundException {
        this.deleteSubscription(id);
    }

    @Override
    @RequestMapping(value = "/vnf-alarm-subscribe", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String subscribe(@Payload AlarmEndpoint endpoint) {
        AlarmEndpoint alarmEndpoint = this.saveSubscription(endpoint);
        return alarmEndpoint.getId();
    }*/
}
