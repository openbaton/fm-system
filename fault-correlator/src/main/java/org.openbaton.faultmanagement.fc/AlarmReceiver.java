package org.openbaton.faultmanagement.fc;

import org.openbaton.faultmanagement.events.notifications.AbstractVNFAlarm;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created by mob on 09.11.15.
 */
@Component
public class AlarmReceiver implements org.openbaton.faultmanagement.fc.interfaces.AlarmReceiver{


    @Override
    @JmsListener(destination = "vnf-alarm", containerFactory = "queueJmsContainerFactory")
    public void receiveVnfAlarm(AbstractVNFAlarm abstractVNFAlarm) {

    }
}
