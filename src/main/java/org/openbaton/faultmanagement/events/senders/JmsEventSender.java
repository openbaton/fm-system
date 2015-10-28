/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.faultmanagement.events.senders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.catalogue.mano.common.faultmanagement.Alarm;
import org.openbaton.faultmanagement.events.AbstractVNFAlarm;
import org.openbaton.faultmanagement.events.VNFAlarmNotification;
import org.openbaton.faultmanagement.events.VNFAlarmStateChangedNotification;
import org.openbaton.faultmanagement.events.senders.interfaces.EventSender;
import org.openbaton.faultmanagement.model.AlarmEndpoint;
import org.openbaton.faultmanagement.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.concurrent.Future;

/**
 * Created by mob on 28/10/15.
 */
@Service
@Scope
public class JmsEventSender implements EventSender {

    @Autowired
    private JmsTemplate jmsTemplate;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    @Async
    public Future<Void> send(AlarmEndpoint endpoint, final AbstractVNFAlarm abstractAlarm) {
        MessageCreator mc=null;
        if(abstractAlarm instanceof VNFAlarmNotification) {
            VNFAlarmNotification vnfAlarmNotification = (VNFAlarmNotification) abstractAlarm;
            final String json = Parser.getMapper().toJson(vnfAlarmNotification.getAlarm(), Alarm.class);
            log.debug("Sending VNF alarm Notification: "+ json);
            mc = createMessageCreator(json);
        }
        else if (abstractAlarm instanceof VNFAlarmStateChangedNotification) {
            VNFAlarmStateChangedNotification vnfAlarmStateChangedNotification = (VNFAlarmStateChangedNotification) abstractAlarm;
            final String json = "{alarmID='"+vnfAlarmStateChangedNotification.getAlarmId()+"', state='"+vnfAlarmStateChangedNotification.getAlarmState()+"'}";
            mc = createMessageCreator(json);
        }
        jmsTemplate.send(endpoint.getEndpoint(), mc);
        return null;
    }

    private MessageCreator createMessageCreator(final String json){
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage objectMessage = session.createTextMessage(json);
                return objectMessage;
            }
        };
        return messageCreator;
    }
}
