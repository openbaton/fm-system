package org.openbaton.faultmanagement.fc.droolsconfig;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.io.ResourceFactory;
import org.openbaton.faultmanagement.fc.NSRManagerImpl;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.policymanagement.MonitoringManagerImpl;
import org.openbaton.faultmanagement.fc.policymanagement.PolicyManagerImpl;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.MonitoringManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * Created by mob on 18.01.16.
 */
@Configuration
public class DroolsAutoConfiguration {

    private static final String RULES_PATH = "rules/";

    @Bean
    KieSession kieSession() throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        for (Resource file : getRuleFiles()) {
            kfs.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
        }

        KieBuilder kbuilder = ks.newKieBuilder(kfs);
        kbuilder.buildAll();

        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalArgumentException(kbuilder.getResults().toString());
        }

        ReleaseId relId = kbuilder.getKieModule().getReleaseId();
        KieContainer kcontainer = ks.newKieContainer(relId);
        KieBaseConfiguration kbconf = ks.newKieBaseConfiguration();
        kbconf.setOption(EventProcessingOption.STREAM);
        // kbconf.setOption(EqualityBehaviorOption.EQUALITY);

        KieBase kbase = kcontainer.newKieBase(kbconf);
        KieSessionConfiguration ksconf = ks.newKieSessionConfiguration();
        KieSession ksession = kbase.newKieSession(ksconf, null);

        return ksession;
    }

    private Resource[] getRuleFiles() throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "**/*.*");
    }
}