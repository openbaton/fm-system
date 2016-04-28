package org.openbaton.faultmanagement.fc.droolsconfig;

import org.kie.api.runtime.KieSession;
import org.openbaton.faultmanagement.fc.interfaces.KieSessionGlobalConfiguration;
import org.openbaton.faultmanagement.fc.interfaces.NFVORequestorWrapper;
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
    private NFVORequestorWrapper NFVORequestorWrapper;

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
        kieSession.setGlobal("nfvoRequestor", NFVORequestorWrapper);

        new Thread(new Runnable() {
            public void run() {
                kieSession.fireUntilHalt();
            }
        }).start();
    }
}
