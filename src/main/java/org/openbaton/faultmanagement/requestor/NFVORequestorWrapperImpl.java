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

package org.openbaton.faultmanagement.requestor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrHealedMessage;
import org.openbaton.catalogue.security.Project;
import org.openbaton.faultmanagement.requestor.interfaces.NFVORequestorWrapper;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.NfvoRequestorBuilder;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NFVORequestorWrapperImpl implements NFVORequestorWrapper {
  private static final Logger log = LoggerFactory.getLogger(NFVORequestorWrapperImpl.class);
  private NFVORequestor nfvoRequestor;

  @Value("${nfvo-usr:admin}")
  private String nfvoUsr;

  @Value("${nfvo-pwd:openbaton}")
  private String nfvoPwd;

  @Value("${nfvo.ip:localhost}")
  private String nfvoIp;

  @Value("${nfvo.port:8080}")
  private int nfvoPort;

  @Value("${nfvo.ssl.enabled:false}")
  private boolean sslEnabled;

  @Value("${server.port:9000}")
  private String fmsPort;

  @Value("${fms.service.key:}")
  private String serviceKey;

  private String projectId;

  @PostConstruct
  public void init() {
    if (nfvoIp == null || nfvoIp.isEmpty()) {
      log.error("The NFVO IP address must not be null!");
      throw new IllegalArgumentException("The NFVO IP address must not be null!");
    }
    if (serviceKey == null || serviceKey.isEmpty()) {
      log.error(
          "Service key is null. Please get the service key from the NFVO and copy it to the property fms.service.key");
      System.exit(1);
    }
    try {
      this.nfvoRequestor =
          NfvoRequestorBuilder.create()
              .nfvoIp(nfvoIp)
              .nfvoPort(nfvoPort)
              .sslEnabled(sslEnabled)
              .serviceName("fms")
              .serviceKey(serviceKey.trim())
              .build();
    } catch (SDKException e) {
      if (log.isDebugEnabled()) log.error(e.getMessage(), e);
      else log.error(e.getMessage());
      System.exit(1);
    }
  }

  @Override
  public NetworkServiceRecord getNsr(String nsrId) throws SDKException {
    nfvoRequestor.setProjectId(getProjectId(nsrId));
    return nfvoRequestor.getNetworkServiceRecordAgent().findById(nsrId);
  }

  @Override
  public boolean nsrExists(String nsrId) throws SDKException {
    nfvoRequestor.setProjectId(getProjectId(nsrId));
    boolean result;
    try {
      result = nfvoRequestor.getNetworkServiceRecordAgent().findById(nsrId) != null;
    } catch (SDKException sdke) {
      if (sdke.getReason().contains("404")) {
        return false;
      } else throw sdke;
    }
    return result;
  }

  private String getProjectId(String nsrId) {
    String projectId = null;
    try {
      for (Project project : nfvoRequestor.getProjectAgent().findAll()) {
        nfvoRequestor.setProjectId(project.getId());
        for (NetworkServiceRecord nsr : nfvoRequestor.getNetworkServiceRecordAgent().findAll())
          if (nsr.getId().equals(nsrId)) projectId = nsr.getProjectId();
      }
    } catch (SDKException e) {
      log.warn("Problem while fetching existing NSRs from the NFVO", e);
    }
    return projectId;
  }

  @Override
  public List<NetworkServiceRecord> getNsrs() {
    List<NetworkServiceRecord> nsrs = new ArrayList<>();
    try {
      for (Project project : nfvoRequestor.getProjectAgent().findAll()) {
        nfvoRequestor.setProjectId(project.getId());
        nsrs.addAll(nfvoRequestor.getNetworkServiceRecordAgent().findAll());
      }
    } catch (SDKException e) {
      log.warn("Problem while fetching existing NSRs from the NFVO", e);
    }
    return nsrs;
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId, String vnfrId)
      throws SDKException {
    nfvoRequestor.setProjectId(getProjectId(nsrId));
    return nfvoRequestor
        .getNetworkServiceRecordAgent()
        .getVirtualNetworkFunctionRecord(nsrId, vnfrId);
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId)
      throws SDKException {
    for (NetworkServiceRecord nsr : getNsrs()) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        if (vnfr.getId().equals(vnfrId)) return vnfr;
      }
    }
    return null;
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(
      String hostname) {
    List<NetworkServiceRecord> nsrs = getNsrs();
    for (NetworkServiceRecord nsr : nsrs) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
          for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
            if (vnfcInstance.getHostname().equals(hostname)) return vnfr;
          }
        }
      }
    }
    return null;
  }

  @Override
  public VNFCInstance getVNFCInstanceFromVnfr(
      VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId) {
    for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
      for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
        if (vnfcInstance.getId().equals(vnfcInstaceId)) return vnfcInstance;
      }
    }
    return null;
  }

  public VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId) {

    for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
      for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
        if (vnfcInstance.getId().equals(vnfcInstaceId)) return vdu;
      }
    }
    return null;
  }

  @Override
  public String subscribe(String projectId, EventEndpoint eventEndpoint) throws SDKException {
    nfvoRequestor.setProjectId(projectId);
    return nfvoRequestor.getEventAgent().create(eventEndpoint).getId();
  }

  @Override
  public String subscribe(EventEndpoint eventEndpoint) {
    String subscriptionId = null;
    try {
      for (Project project : nfvoRequestor.getProjectAgent().findAll()) {
        nfvoRequestor.setProjectId(project.getId());
        subscriptionId = nfvoRequestor.getEventAgent().create(eventEndpoint).getId();
      }
    } catch (Exception e) {
      log.error("Problem while fetching existing NSRs from the NFVO", e);
    }
    return subscriptionId;
  }

  @Override
  public void deleteVnfcInstance(String nsrId, String vnfrId, String vduId, String vnfcInstanceId)
      throws SDKException {
    nfvoRequestor.setProjectId(getProjectId(nsrId));
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .deleteVNFCInstance(nsrId, vnfrId, vduId, vnfcInstanceId);
  }

  @Override
  public void createStandbyVNFCInstance(
      String nsrId,
      String vnfrId,
      String vduId,
      VNFComponent vnfComponent,
      ArrayList<String> vimInstanceNames)
      throws SDKException {
    //setProjectId();
    log.debug("sending: vnf component: " + vnfComponent);
    log.debug("sending: vimInstanceNames: " + vimInstanceNames);
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .createVNFCInstanceInStandby(nsrId, vnfrId, vduId, vnfComponent, vimInstanceNames);
  }

  @Override
  public void switchToStandby(
      String nsrId, String vnfrId, String vduId, String vnfcId, VNFCInstance failedVnfcInstance)
      throws SDKException {
    nfvoRequestor.setProjectId(getProjectId(nsrId));
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .switchToStandby(nsrId, vnfrId, vduId, vnfcId, failedVnfcInstance);
  }

  @Override
  public void unSubscribe(String id) throws SDKException {
    nfvoRequestor.getEventAgent().delete(id);
  }

  public void executeHeal(
      String nsrId, String vnfrId, String vduId, String failedVnfcInstanceId, String cause)
      throws SDKException {
    NFVMessage nfvMessage = getHealMessage(cause);
    nfvoRequestor.setProjectId(getProjectId(nsrId));
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .postAction(nsrId, vnfrId, vduId, failedVnfcInstanceId, nfvMessage);
  }

  private VnfmOrHealedMessage getHealMessage(String cause) {
    VnfmOrHealedMessage vnfmOrHealVNFRequestMessage = new VnfmOrHealedMessage();
    vnfmOrHealVNFRequestMessage.setAction(Action.HEAL);
    vnfmOrHealVNFRequestMessage.setCause(cause);
    return vnfmOrHealVNFRequestMessage;
  }

  @Override
  public VNFCInstance getVNFCInstance(String hostname) {
    List<NetworkServiceRecord> nsrs = getNsrs();
    for (NetworkServiceRecord nsr : nsrs) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
          for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
            if (vnfcInstance.getHostname().equals(hostname)) return vnfcInstance;
          }
        }
      }
    }
    return null;
  }

  @Override
  public VNFCInstance getVNFCInstanceById(String VnfcId) {

    List<NetworkServiceRecord> nsrs = getNsrs();
    for (NetworkServiceRecord nsr : nsrs) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
          for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
            if (vnfcInstance.getId().equals(VnfcId)) return vnfcInstance;
          }
        }
      }
    }
    return null;
  }
}
