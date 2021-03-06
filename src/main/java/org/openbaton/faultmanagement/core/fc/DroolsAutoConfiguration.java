/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
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

package org.openbaton.faultmanagement.core.fc;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/** Created by mob on 18.01.16. */
@Configuration
public class DroolsAutoConfiguration {

  private static final String RULES_PATH = "rules/";
  private static final Logger log = LoggerFactory.getLogger(DroolsAutoConfiguration.class);

  @Bean
  KieSession kieSession() throws IOException {
    KieServices ks = KieServices.Factory.get();
    KieFileSystem kfs = ks.newKieFileSystem();

    try {
      for (Resource file : getRuleFiles()) {
        log.debug("Rule: " + file.getFilename());
        kfs.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
      }
    } catch (IOException e) {
      log.error("Problem accessing rules in " + RULES_PATH);
      throw e;
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
