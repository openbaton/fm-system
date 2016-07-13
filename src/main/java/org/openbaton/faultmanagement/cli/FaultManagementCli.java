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

package org.openbaton.faultmanagement.cli;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.openbaton.catalogue.mano.common.monitoring.VNFAlarm;
import org.openbaton.catalogue.mano.common.monitoring.VRAlarm;
import org.openbaton.faultmanagement.repo.VNFAlarmRepository;
import org.openbaton.faultmanagement.repo.VRAlarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by mob on 28.02.16.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class FaultManagementCli implements CommandLineRunner {
  private final static Map<String, String> helpCommandList =
      new HashMap<String, String>() {
        {
          put("help", "Print the usage");
          put("exit", "Exit the application");
          put("vrAlarms", "print all current Virtualized Resource alarms");
          put("vnfAlarms", "print all current Virtual Network Function alarms");
        }
      };
  protected Logger log = LoggerFactory.getLogger(this.getClass());
  private int ruleVersion;
  @Autowired private VRAlarmRepository vrAlarmRepository;
  @Autowired private VNFAlarmRepository vnfAlarmRepository;
  @Autowired private KieSession kieSession;

  private static void exit(int status) {
    System.exit(status);
  }

  public static void usage() {
    System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
    System.out.println("Available commands are");

    for (Map.Entry<String, String> entry : helpCommandList.entrySet()) {
      String format = "%-80s%s%n";
      System.out.printf(format, entry.getKey() + ":", entry.getValue());
    }
    System.out.println("/~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~/");
  }

  @Override
  public void run(String... args) throws Exception {
    ConsoleReader reader = null;
    try {
      reader = new ConsoleReader();
    } catch (IOException e) {
      log.error("Oops, Error while creating ConsoleReader");
      exit(999);
    }
    ruleVersion = 1;
    String line;
    PrintWriter out = new PrintWriter(reader.getOutput());
    List<Completer> completors = new ArrayList<>();
    completors.add(new StringsCompleter(helpCommandList.keySet()));
    completors.add(new FileNameCompleter());
    reader.addCompleter(new ArgumentCompleter(completors));
    reader.setPrompt(
        "\u001B[135m" + System.getProperty("user.name") + "@[\u001B[32mFM-System\u001B[0m]~> ");
    while ((line = reader.readLine()) != null) {
      try {
        out.flush();
        line = line.trim();
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
          exit(0);
        } else if (line.equalsIgnoreCase("clear")) {
          reader.clearScreen();
        } else if (line.equalsIgnoreCase("help")) {
          usage();
        } else if (line.startsWith("vrAlarms")) {
          listVRAlarms(line);
        } else if (line.startsWith("vnfAlarms")) {
          listVnfAlarms(line);
        } else if (line.startsWith("updateRules")) {
          updateDroolsRules();
        } else if (line.equalsIgnoreCase("")) {
          continue;
        } else usage();
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  private void updateDroolsRules() {
    KieServices ks = KieServices.Factory.get();
    ruleVersion++;
    ReleaseId relId = ks.newReleaseId("org.kie", "rule-upgrade", "" + ruleVersion);
    KieContainer kcontainer = ks.newKieContainer(relId);
    KieBase kieBase = kieSession.getKieBase();
  }

  private void listVnfAlarms(String line) {
    Iterable<VNFAlarm> alarms = vnfAlarmRepository.findAll();
    if (!alarms.iterator().hasNext()) {
      System.out.println("No vnfAlarms in the history");
      return;
    }
    for (VNFAlarm vnfAlarm : alarms) {
      String alarmId = vnfAlarm.getAlarmId();
      String vimInstanceName = vnfAlarm.getVimName();
      String alarmRaisedTime = vnfAlarm.getAlarmRaisedTime();
      String eventTime = vnfAlarm.getEventTime();
      String faultType =
          vnfAlarm.getFaultType() == null ? null : vnfAlarm.getFaultType().toString();
      String alarmState =
          vnfAlarm.getAlarmState() == null ? null : vnfAlarm.getAlarmState().toString();
      String perceivedSeverity =
          vnfAlarm.getPerceivedSeverity() == null
              ? null
              : vnfAlarm.getPerceivedSeverity().toString();

      System.out.println(
          "AlarmId:'"
              + alarmId
              + "' vimInstanceName:'"
              + vimInstanceName
              + "' alarmRaisedTime:'"
              + alarmRaisedTime
              + "' faultDetectedTime:'"
              + eventTime
              + "' faultType:'"
              + faultType
              + "' alarmState:"
              + alarmState
              + "' perceivedSeverity: "
              + perceivedSeverity);
    }
  }

  private void listVRAlarms(String line) {
    Iterable<VRAlarm> alarms = vrAlarmRepository.findAll();
    if (!alarms.iterator().hasNext()) {
      System.out.println("No vrAlarms in the history");
      return;
    }
    for (VRAlarm vrAlarm : alarms) {
      String alarmId = vrAlarm.getAlarmId();
      String managedObject = vrAlarm.getManagedObject();
      String alarmRaisedTime = vrAlarm.getAlarmRaisedTime();
      String eventTime = vrAlarm.getEventTime();
      String faultType = vrAlarm.getFaultType() == null ? null : vrAlarm.getFaultType().toString();
      String alarmState =
          vrAlarm.getAlarmState() == null ? null : vrAlarm.getAlarmState().toString();
      String perceivedSeverity =
          vrAlarm.getPerceivedSeverity() == null ? null : vrAlarm.getPerceivedSeverity().toString();
      System.out.println(
          "AlarmId:'"
              + alarmId
              + "' managedObject:'"
              + managedObject
              + "' alarmRaisedTime:'"
              + alarmRaisedTime
              + "' faultDetectedTime:'"
              + eventTime
              + "' faultType:'"
              + faultType
              + "' alarmState:"
              + alarmState
              + "' perceivedSeverity:"
              + perceivedSeverity);
    }
  }
}
