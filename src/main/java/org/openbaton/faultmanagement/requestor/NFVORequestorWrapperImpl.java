package org.openbaton.faultmanagement.requestor;

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
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

import static org.kie.internal.runtime.manager.audit.query.RequestInfoQueryBuilder.OrderBy.id;

@Service
public class NFVORequestorWrapperImpl implements NFVORequestorWrapper {
  private static final Logger log = LoggerFactory.getLogger(NFVORequestorWrapperImpl.class);
  private NFVORequestor nfvoRequestor;

  @Value("${nfvo-usr:}")
  private String nfvoUsr;

  @Value("${nfvo-pwd:}")
  private String nfvoPwd;

  @Value("${nfvo.ip:}")
  private String nfvoIp;

  @Value("${nfvo.port:8080}")
  private String nfvoPort;

  @Value("${server.port:}")
  private String fmsPort;

  private String projectId;

  @PostConstruct
  public void init() throws IOException {
    this.nfvoRequestor = new NFVORequestor(nfvoUsr, nfvoPwd, null, false, nfvoIp, nfvoPort, "1");
    try {
      log.debug("executing get all projects");
      List<Project> projects = nfvoRequestor.getProjectAgent().findAll();
      log.debug("found " + projects.size() + " projects");

      for (Project project : projects) {
        if (project.getName().equals("default")) {
          projectId = project.getId();
        }
      }
    } catch (ClassNotFoundException | SDKException e) {
      e.printStackTrace();
    }
  }

  @Override
  public NetworkServiceRecord getNsr(String nsrId) throws ClassNotFoundException, SDKException {
    nfvoRequestor.setProjectId(projectId);
    return nfvoRequestor.getNetworkServiceRecordAgent().findById(nsrId);
  }

  @Override
  public List<NetworkServiceRecord> getNsrs() throws ClassNotFoundException, SDKException {
    nfvoRequestor.setProjectId(projectId);
    return nfvoRequestor.getNetworkServiceRecordAgent().findAll();
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId, String vnfrId)
      throws SDKException {
    nfvoRequestor.setProjectId(projectId);
    return nfvoRequestor
        .getNetworkServiceRecordAgent()
        .getVirtualNetworkFunctionRecord(nsrId, vnfrId);
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId)
      throws SDKException, ClassNotFoundException {
    for (NetworkServiceRecord nsr : getNsrs()) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        if (vnfr.getId().equals(vnfrId)) return vnfr;
      }
    }
    return null;
  }

  @Override
  public VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(
      String hostname) throws SDKException, ClassNotFoundException {
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
  public String subscribe(EventEndpoint eventEndpoint) throws SDKException {
    nfvoRequestor.setProjectId(projectId);
    EventEndpoint response = nfvoRequestor.getEventAgent().create(eventEndpoint);
    return response.getId();
  }

  @Override
  public void deleteVnfcInstance(String nsrId, String vnfrId, String vduId, String vnfcInstanceId)
      throws SDKException {
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .deleteVNFCInstance(nsrId, vnfrId, vduId, vnfcInstanceId);
  }

  @Override
  public void createStandbyVNFCInstance(
      String nsrId, String vnfrId, String vduId, VNFComponent vnfComponent) throws SDKException {
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .createVNFCInstanceInStandby(nsrId, vnfrId, vduId, vnfComponent);
  }

  @Override
  public void switchToStandby(
      String nsrId, String vnfrId, String vduId, String vnfcId, VNFCInstance failedVnfcInstance)
      throws SDKException {
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .switchToStandby(nsrId, vnfrId, vduId, vnfcId, failedVnfcInstance);
  }

  @Override
  public void unSubscribe(String id) throws SDKException {
    nfvoRequestor.setProjectId(projectId);
    nfvoRequestor.getEventAgent().delete(id);
  }

  public void executeHeal(
      String nsrid, String vnfrId, String vduId, String failedVnfcInstanceId, String cause)
      throws SDKException {
    NFVMessage nfvMessage = getHealMessage(cause);
    nfvoRequestor
        .getNetworkServiceRecordAgent()
        .postAction(nsrid, vnfrId, vduId, failedVnfcInstanceId, nfvMessage);
  }

  private VnfmOrHealedMessage getHealMessage(String cause) {
    VnfmOrHealedMessage vnfmOrHealVNFRequestMessage = new VnfmOrHealedMessage();
    vnfmOrHealVNFRequestMessage.setAction(Action.HEAL);
    vnfmOrHealVNFRequestMessage.setCause(cause);
    return vnfmOrHealVNFRequestMessage;
  }

  @Override
  public VNFCInstance getVNFCInstance(String hostname) throws SDKException, ClassNotFoundException {

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
  public VNFCInstance getVNFCInstanceById(String VnfcId)
      throws SDKException, ClassNotFoundException {

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
