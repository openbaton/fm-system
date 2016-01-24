package org.openbaton.faultmanagement.fc.droolsconfig;

import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.openbaton.faultmanagement.fc.interfaces.KieSessionGlobalConfiguration;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.fc.repositories.AlarmRepository;
import org.openbaton.faultmanagement.ha.HighAvailabilityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by mob on 21.01.16.
 */
@Service
public class KieSessionGlobalConfigurationImpl implements KieSessionGlobalConfiguration {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private HighAvailabilityManager highAvailabilityManager;

    @Autowired
    private KieSession kieSession;

    @Autowired
    private NSRManager nsrManager;

    @Autowired
    private PolicyManager policyManager;

    private static final Logger log = LoggerFactory.getLogger(KieSessionGlobalConfigurationImpl.class);

    @PostConstruct
    public void init() {



        kieSession.setGlobal("policyManager", policyManager);
        kieSession.setGlobal("logger", log);
        kieSession.setGlobal("alarmRepository", alarmRepository);
        kieSession.setGlobal("highAvailabilityManager", highAvailabilityManager);
        kieSession.setGlobal("nsrManager", nsrManager);
        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();



        /*VRAlarm vrAlarm = new VRAlarm();
        vrAlarm.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vrAlarm.setManagedObject("iperf-server-280");
        vrAlarm.setAlarmState(AlarmState.FIRED);
        vrAlarm.setThresholdId("nouuuuuu");

        log.debug("Coming the VR alarm: "+vrAlarm);
        log.debug(" --- Starting Drools rules --- ");
        kieSession.insert(vrAlarm);
        kieSession.fireAllRules();
        log.debug(" --- Ended Drools rules --- ");*/
    }
}
