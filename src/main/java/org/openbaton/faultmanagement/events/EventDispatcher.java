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

package org.openbaton.faultmanagement.events;

/**
 * Created by lto on 03/06/15.
 */

import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.events.senders.interfaces.AlarmDispatcher;
import org.openbaton.faultmanagement.events.senders.interfaces.EventSender;
import org.openbaton.faultmanagement.model.AlarmEndpoint;
import org.openbaton.faultmanagement.repo.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * This class implements the interface {@Link EventDispatcher} so is in charge
 * of handling the de/registration of a EventEndpoint.
 * <p/>
 * Moreover receives also internal events and dispatches them to the external applications.
 */
@Service
@Scope
@EnableJms
class EventDispatcher implements ApplicationListener<AbstractVNFAlarm>, AlarmDispatcher{

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ConfigurableApplicationContext context;

    @Override
    @JmsListener(destination = "vnf-alarm-register", containerFactory = "queueJmsContainerFactory")
    public AlarmEndpoint register(@Payload AlarmEndpoint endpoint) {
        AlarmEndpoint alarmEndpoint = subscriptionRepository.save(endpoint);
        log.info("Registered alarm endpoint:" + alarmEndpoint);
        return alarmEndpoint;
    }

    @Override
    public void onApplicationEvent(AbstractVNFAlarm event) {
        log.debug("Received event: " + event);
        dispatchEvent(event);
    }

    @Override
    public void dispatchEvent(AbstractVNFAlarm abstractAlarm) {
        log.debug("dispatching Alarm to the world!!!");
        log.debug("event is: " + abstractAlarm);

        Iterable<AlarmEndpoint> alarmEndpoints = subscriptionRepository.findAll();

        for (AlarmEndpoint alarmEndpoint : alarmEndpoints) {
            log.debug("Checking endpoint: " + alarmEndpoint);
            if(abstractAlarm.getAlarm().getVnfd().getName().equalsIgnoreCase(alarmEndpoint.getVirtualNetworkFunctionId()) &&
                    abstractAlarm.getAlarm().getPerceivedSeverity().ordinal()>= alarmEndpoint.getPerceivedSeverity().ordinal()){
                    log.debug("Dispatching event to endpoint: "+alarmEndpoint.getName());
                    sendAlarm(alarmEndpoint, abstractAlarm);
            }
        }

    }

    private void sendAlarm(AlarmEndpoint endpoint, AbstractVNFAlarm event) {
        EventSender sender = (EventSender) context.getBean(endpoint.getType().toString().toLowerCase() + "EventSender");
        log.trace("Sender is: " + sender.getClass().getSimpleName());
        try {
            sender.send(endpoint, event);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while dispatching event " + event);
        }
    }

    @Override
    @JmsListener(destination = "vnf-alarm-unregister", containerFactory = "queueJmsContainerFactory")
    public void unregister(String id) throws NotFoundException {
        if (subscriptionRepository.exists(id))
            subscriptionRepository.delete(id);
    }

}
