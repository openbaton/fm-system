package org.openbaton.faultmanagement.fc.droolsconfig;

import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.AlarmType;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.openbaton.faultmanagement.fc.interfaces.KieSessionGlobalConfiguration;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.fc.repositories.VNFAlarmRepository;
import org.openbaton.faultmanagement.fc.repositories.VRAlarmRepository;
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
    private VRAlarmRepository vrAlarmRepository;

    @Autowired
    private VNFAlarmRepository vnfAlarmRepository;

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
        kieSession.setGlobal("vrAlarmRepository", vrAlarmRepository);
        kieSession.setGlobal("vnfAlarmRepository", vnfAlarmRepository);
        kieSession.setGlobal("highAvailabilityManager", highAvailabilityManager);
        kieSession.setGlobal("nsrManager", nsrManager);

        VRAlarm vrAlarm = new VRAlarm();
        vrAlarm.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vrAlarm.setManagedObject("iperf-server-280");
        vrAlarm.setAlarmState(AlarmState.FIRED);
        vrAlarm.setThresholdId("nou");

        VRAlarm vnfAlarm = new VRAlarm();
        vnfAlarm.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vnfAlarm.setManagedObject("iperf-server-280");
        vnfAlarm.setAlarmState(AlarmState.FIRED);
        vnfAlarm.setThresholdId("nou");
        vnfAlarm.setAlarmType(AlarmType.VIRTUAL_NETWORK_FUNCTION);

        VirtualizedResourceAlarmStateChangedNotification virtualizedResourceAlarmStateChangedNotification = new VirtualizedResourceAlarmStateChangedNotification();
        virtualizedResourceAlarmStateChangedNotification.setAlarmState(AlarmState.CLEARED);
        virtualizedResourceAlarmStateChangedNotification.setTriggerId("nou");

       /* log.debug("Coming the VR alarm: "+vrAlarm);
        log.debug(" --- Starting Drools rules --- ");
        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.insert(vrAlarm);
        kieSession.fireAllRules();
        log.debug(" --- Ended Drools rules --- ");


        log.debug(" --- Starting Drools rules 2 --- ");
        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.insert(virtualizedResourceAlarmStateChangedNotification);
        kieSession.fireAllRules();
        log.debug(" --- Ended Drools rules 2 --- ");


        log.debug(" --- Starting Drools rules 3 --- ");
        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.insert(vnfAlarm);
        kieSession.fireAllRules();
        log.debug(" --- Ended Drools rules 3 --- ");*/
    }
}
