package org.openbaton.faultmanagement.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.catalogue.mano.common.faultmanagement.PerceivedSeverity;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.events.notifications.AbstractVNFAlarm;
import org.openbaton.faultmanagement.model.AlarmEndpoint;

import java.util.List;

/**
 * Created by mob on 27.10.15.
 */
public interface VNFFaultManagement {
    AlarmEndpoint subscribe(AlarmEndpoint endpoint);
    void unsubscribe(String alarmEndpointId) throws NotFoundException;
    void notify(AlarmEndpoint endpoint, AbstractVNFAlarm event);
    List<Alarm> getAlarmList(String vnfId, PerceivedSeverity perceivedSeverity);
}
