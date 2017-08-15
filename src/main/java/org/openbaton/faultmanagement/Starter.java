package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.faultmanagement.core.mm.interfaces.MonitoringManager;
import org.openbaton.faultmanagement.core.pm.interfaces.PolicyManager;
import org.openbaton.faultmanagement.requestor.interfaces.NFVORequestorWrapper;
import org.openbaton.faultmanagement.subscriber.interfaces.EventSubscriptionManger;
import org.openbaton.faultmanagement.utils.Utils;
import org.openbaton.monitoring.interfaces.MonitoringPluginCaller;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/** Created by mob on 22/02/2017. */
@Service
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties
public class Starter implements CommandLineRunner, ApplicationListener<ContextClosedEvent> {

  @Autowired private EventSubscriptionManger eventSubscriptionManger;
  @Autowired private MonitoringManager monitoringManager;
  @Autowired private PolicyManager policyManager;
  @Autowired private NFVORequestorWrapper nfvoRequestor;
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${nfvo.ip:}")
  private String nfvoIp;

  @Value("${nfvo.port:8080}")
  private String nfvoPort;

  @Value("${spring.rabbitmq.username:}")
  private String rabbitmqUsr;

  @Value("${spring.rabbitmq.password:}")
  private String rabbitmqPwd;

  @Value("${spring.rabbitmq.host:}")
  private String rabbitmqIp;

  @Value("${spring.rabbitmq.management.port:}")
  private String rabbitmqManagementPort;

  @Value("${spring.rabbitmq.port:}")
  private String rabbitmqPort;

  @Value("${plugin.monitoring.name:}")
  private String monitoringPluginName;

  @Value("${plugin.monitoring.type:}")
  private String monitoringPluginType;

  private MonitoringPluginCaller monitoringPluginCaller;

  private void waitForNfvo() {
    if (!Utils.isNfvoStarted(nfvoIp, nfvoPort)) {
      log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
      System.exit(1);
    }
  }

  private void waitForMonitoringPlugin() throws InterruptedException {
    while (!loadMonitoringPluginCaller()) {
      log.info(
          "Waiting until Monitoring Plugin (of type " + monitoringPluginType + ") is available...");
      Thread.sleep(5000);
    }
    log.debug("MonitoringPluginCaller obtained");
  }

  private boolean loadMonitoringPluginCaller() {
    try {
      monitoringPluginCaller =
          new MonitoringPluginCaller(
              rabbitmqIp,
              rabbitmqUsr,
              rabbitmqPwd,
              Integer.parseInt(rabbitmqPort),
              "",
              monitoringPluginType,
              monitoringPluginName,
              rabbitmqManagementPort,
              120000);
    } catch (Exception e) {
      return false;
    }
    monitoringManager.setMonitoringPluginCaller(monitoringPluginCaller);
    return true;
  }

  @Override
  public void run(String... args) throws Exception {
    // wait until NFVO availability
    waitForNfvo();

    // wait until monitoring plugin availability
    waitForMonitoringPlugin();

    // subscription to NFVO
    eventSubscriptionManger.subscribeToNFVO();
    // manage already existing NSRs
    for (NetworkServiceRecord nsr : nfvoRequestor.getNsrs()) {
      policyManager.manageNSR(nsr);
    }
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    //unsubscribe to NFVO
    try {
      eventSubscriptionManger.unSubscribeToNFVO();
    } catch (SDKException | ClassNotFoundException e) {
      log.error("Unsubsctiption to NFVO failed: " + e.getMessage());
    }
  }
}
