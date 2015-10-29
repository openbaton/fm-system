package org.openbaton.faultmanagement.events.senders.interfaces;

import org.openbaton.faultmanagement.events.notifications.AbstractVNFAlarm;
import org.openbaton.faultmanagement.model.AlarmEndpoint;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by mob on 28.10.15.
 */
public interface EventSender {
    Future<Void> send(AlarmEndpoint endpoint, AbstractVNFAlarm event) throws IOException;

}
