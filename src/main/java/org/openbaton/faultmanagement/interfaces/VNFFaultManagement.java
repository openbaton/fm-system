package org.openbaton.faultmanagement.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.AbstractVNFAlarm;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmEndpoint;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.exceptions.NotFoundException;

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
