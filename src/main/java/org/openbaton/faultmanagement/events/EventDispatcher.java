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
 * Created by mob on 28/10/15.
 */

/**
 * This class implements the interface {@Link EventDispatcher} so is in charge
 * of handling the de/registration of a EventEndpoint.
 * <p/>
 * Moreover receives also internal events and dispatches them to the external applications.
 */
//@Service
public class EventDispatcher /*implements ApplicationListener<AbstractVNFAlarm>, AlarmDispatcher*/{

    /*private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private AlarmRepository alarmRepository;
    @Autowired
    private SubscriptionRegister subscriptionRegister;


    @Override
    public void onApplicationEvent(AbstractVNFAlarm event) {
        log.debug("Received event: " + event);
        dispatchAlarm(event);
    }

    @Override
    public void dispatchAlarm(AbstractVNFAlarm abstractAlarm) {

        log.debug("event is: " + abstractAlarm);
        Alarm alarm = alarmRepository.save(abstractAlarm.getAlarm());
        log.debug("Alarm saved: "+alarm);
        Iterable<AlarmEndpoint> alarmEndpoints = subscriptionRegister.getSubscriptionRepository().findAll();

        for (AlarmEndpoint alarmEndpoint : alarmEndpoints) {
            log.debug("Checking endpoint: " + alarmEndpoint);
            if(abstractAlarm.getAlarm().getResourceId().equalsIgnoreCase(alarmEndpoint.getVirtualNetworkFunctionId()) &&
                    abstractAlarm.getAlarm().getPerceivedSeverity().ordinal()>= alarmEndpoint.getPerceivedSeverity().ordinal()){
                    log.debug("Dispatching event to endpoint: "+alarmEndpoint.getName());
                    notify(alarmEndpoint, abstractAlarm);
            }
        }

    }

    public void notify(AlarmEndpoint endpoint, AbstractVNFAlarm event) {
        EventSender sender = (EventSender) context.getBean(endpoint.getType().toString().toLowerCase() + "EventSender");
        log.trace("Sender is: " + sender.getClass().getSimpleName());
        try {
            sender.send(endpoint, event);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error while dispatching event " + event);
        }
    }

    public List<Alarm> getAlarmList(String vnfId, PerceivedSeverity perceivedSeverity) {
        return alarmRepository.findByVnfrIdAndPerceivedSeverity(vnfId,perceivedSeverity);
    }
*/
}
