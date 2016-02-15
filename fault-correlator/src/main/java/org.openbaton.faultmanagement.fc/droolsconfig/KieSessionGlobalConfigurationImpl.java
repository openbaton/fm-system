package org.openbaton.faultmanagement.fc.droolsconfig;

import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.AlarmType;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.openbaton.faultmanagement.fc.RecoveryAction;
import org.openbaton.faultmanagement.fc.RecoveryActionStatus;
import org.openbaton.faultmanagement.fc.RecoveryActionType;
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

        /*VRAlarm vrAlarm = new VRAlarm();
        vrAlarm.setThresholdId("123");
        vrAlarm.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vrAlarm.setManagedObject("iperf-server-280");
        vrAlarm.setAlarmState(AlarmState.FIRED);*/


        /*VRAlarm vnfAlarm = new VRAlarm();
        vnfAlarm.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vnfAlarm.setManagedObject("iperf-server-280");
        vnfAlarm.setAlarmState(AlarmState.FIRED);
        vnfAlarm.setThresholdId("nou");
        vnfAlarm.setAlarmType(AlarmType.VIRTUAL_NETWORK_FUNCTION);

        VirtualizedResourceAlarmStateChangedNotification virtualizedResourceAlarmStateChangedNotification = new VirtualizedResourceAlarmStateChangedNotification();
        virtualizedResourceAlarmStateChangedNotification.setAlarmState(AlarmState.CLEARED);
        virtualizedResourceAlarmStateChangedNotification.setTriggerId("nou");*/

        /*log.debug("Coming the VR alarm: "+vrAlarm);
        log.debug(" --- Starting Drools rules --- ");
        kieSession.getAgenda().getAgendaGroup( "pre-rules" ).setFocus();
        kieSession.insert(vrAlarm);
        kieSession.fireAllRules();
        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.fireAllRules();

        log.debug(" --- Ended Drools rules --- ");

        log.debug(" --- Starting Drools rules 2 --- ");

        VRAlarm vrAlarm1 = new VRAlarm();
        vrAlarm1.setThresholdId("321");
        vrAlarm1.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vrAlarm1.setManagedObject("iperf-222222");
        vrAlarm1.setAlarmState(AlarmState.FIRED);

        RecoveryAction recoveryAction = new RecoveryAction(RecoveryActionType.SWITCH_TO_STANDBY,"raer","awd");
        recoveryAction.setStatus(RecoveryActionStatus.FINISHED);
        kieSession.getAgenda().getAgendaGroup( "resolution" ).setFocus();
        kieSession.insert(recoveryAction);
        kieSession.fireAllRules();



        kieSession.getAgenda().getAgendaGroup( "pre-rules" ).setFocus();
        kieSession.insert(vrAlarm1);
        kieSession.fireAllRules();
        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.fireAllRules();

        log.debug(" --- Ended Drools rules 2 --- ");

        log.debug(" --- Starting Drools rules 3 --- ");
        VRAlarm vrAlarm2 = new VRAlarm();
        vrAlarm2.setThresholdId("123213123");
        vrAlarm2.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vrAlarm2.setManagedObject("iperf-2dad");
        vrAlarm2.setAlarmState(AlarmState.FIRED);

        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.insert(vrAlarm2);
        kieSession.fireAllRules();
        log.debug(" --- Ended Drools rules 3 --- ");


        log.debug(" --- Starting Drools rules 4 --- ");
        VRAlarm vrAlarm4 = new VRAlarm();
        vrAlarm4.setThresholdId("1232wdad123");
        vrAlarm4.setPerceivedSeverity(PerceivedSeverity.CRITICAL);
        vrAlarm4.setManagedObject("iperf-dawdawdad");
        vrAlarm4.setAlarmState(AlarmState.FIRED);

        kieSession.getAgenda().getAgendaGroup( "correlation" ).setFocus();
        kieSession.insert(vrAlarm4);
        kieSession.fireAllRules();
        log.debug(" --- Ended Drools rules 4 --- ");*/
    }
}
