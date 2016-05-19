/*
* Copyright (c) 2015-2016 Fraunhofer FOKUS
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.openbaton.faultmanagement.fc.droolsconfig;

import org.kie.api.runtime.KieSession;

import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.openbaton.faultmanagement.fc.interfaces.KieSessionGlobalConfiguration;
import org.openbaton.faultmanagement.fc.interfaces.NFVORequestorWrapper;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.fc.repositories.VNFAlarmRepository;
import org.openbaton.faultmanagement.ha.HighAvailabilityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by mob on 21.01.16.
 */
@Service
public class KieSessionGlobalConfigurationImpl implements KieSessionGlobalConfiguration,ApplicationListener<ContextClosedEvent> {

    @Autowired
    private org.openbaton.faultmanagement.fc.repositories.VRAlarmRepository vrAlarmRepository;

    @Autowired
    private VNFAlarmRepository vnfAlarmRepository;

    @Autowired
    private HighAvailabilityManager highAvailabilityManager;

    @Autowired
    private KieSession kieSession;

    @Autowired
    private NFVORequestorWrapper nfvoRequestorWrapper;

    @Autowired
    private PolicyManager policyManager;

    private static final Logger log = LoggerFactory.getLogger(KieSessionGlobalConfigurationImpl.class);

    @PostConstruct
    public void init() {

        kieSession.setGlobal("policyManager", policyManager);
        kieSession.setGlobal("logger", log);
        kieSession.setGlobal("vrAlarmRepository", vrAlarmRepository);
        kieSession.setGlobal("vnfAlarmRepository", vnfAlarmRepository);
        kieSession.setGlobal("highAvailabilityManager", highAvailabilityManager);
        kieSession.setGlobal("nfvoRequestorWrapper", nfvoRequestorWrapper);

        new Thread(new Runnable() {
            public void run() {
                kieSession.fireUntilHalt();
            }
        }).start();

    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        kieSession.halt();
        kieSession.dispose();
    }
}
