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

package org.openbaton.faultmanagement.core.pm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.catalogue.ManagedNetworkServiceRecord;
import org.openbaton.faultmanagement.core.ham.exceptions.HighAvailabilityException;
import org.openbaton.faultmanagement.core.ham.interfaces.HighAvailabilityManager;
import org.openbaton.faultmanagement.core.mm.interfaces.MonitoringManager;
import org.openbaton.faultmanagement.core.pm.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.core.pm.interfaces.PolicyManager;
import org.openbaton.faultmanagement.repo.ManagedNetworkServiceRecordRepository;
import org.openbaton.faultmanagement.requestor.interfaces.NFVORequestorWrapper;
import org.openbaton.faultmanagement.subscriber.interfaces.EventSubscriptionManger;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by mob on 29.10.15. */
@Service
public class PolicyManagerImpl implements PolicyManager {

  private static final Logger log = LoggerFactory.getLogger(PolicyManagerImpl.class);

  @Autowired private NFVORequestorWrapper nfvoRequestorWrapper;

  @Autowired private MonitoringManager monitoringManager;

  @Autowired private EventSubscriptionManger eventSubscriptionManger;

  @Autowired private HighAvailabilityManager highAvailabilityManager;

  @Autowired private ManagedNetworkServiceRecordRepository mnsrRepo;

  @Override
  public void manageNSR(NetworkServiceRecord nsr)
      throws SDKException, FaultManagementPolicyException, HighAvailabilityException,
          ClassNotFoundException, FileNotFoundException {
    if (!nsrNeedsMonitoring(nsr)) {
      log.info("The NSR" + nsr.getName() + " does not need fault management");
      return;
    }
    log.info("The NSR" + nsr.getName() + " needs fault management monitoring");
    saveManagedNetworkServiceRecord(nsr);

    for (VirtualNetworkFunctionRecord vnfr : getVnfrRequiringFaultManagement(nsr))
      // subscribe for HEAL method
      eventSubscriptionManger.subscribe(vnfr, Action.HEAL);
    // subscribe for SWITCH TO STANDBY
    eventSubscriptionManger.subscribe(nsr, Action.HEAL);
    monitoringManager.startMonitorNS(nsr);
    highAvailabilityManager.configureRedundancy(nsr);
  }

  private void saveManagedNetworkServiceRecord(NetworkServiceRecord nsr) {
    if (mnsrRepo.findByNsrId(nsr.getId()) == null) {
      ManagedNetworkServiceRecord mnsr = new ManagedNetworkServiceRecord();
      mnsr.setNsrId(nsr.getId());
      mnsrRepo.save(mnsr);
    }
  }

  private List<VirtualNetworkFunctionRecord> getVnfrRequiringFaultManagement(
      NetworkServiceRecord nsr) {
    List<VirtualNetworkFunctionRecord> result = new ArrayList<>();
    for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
      for (VirtualDeploymentUnit vdu : vnfr.getVdu())
        if (vdu.getFault_management_policy() != null
            && !vdu.getFault_management_policy().isEmpty()) {
          log.info("VNF " + vnfr.getName() + " requires fault management");
          result.add(vnfr);
        }
    }
    return result;
  }

  private boolean nsrNeedsMonitoring(NetworkServiceRecord nsr)
      throws FaultManagementPolicyException {
    if (nsr.getFaultManagementPolicy() != null && !nsr.getFaultManagementPolicy().isEmpty())
      return true;
    for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
      for (VirtualDeploymentUnit vdu : vnfr.getVdu())
        if (vdu.getFault_management_policy() != null
            && !vdu.getFault_management_policy().isEmpty()) {
          if (vdu.getMonitoring_parameter() != null && !vdu.getMonitoring_parameter().isEmpty())
            return true;
        }
    }
    return false;
  }

  @Override
  public boolean isAManagedAlarm(String triggerId) {
    String policyId = monitoringManager.getPolicyIdFromTrhresholdId(triggerId);
    if (policyId == null) return false;
    return true;
  }

  @Override
  public void unManageNSR(NetworkServiceRecord networkServiceRecord) throws MonitoringException {
    log.debug("stopping threads pf nsr : " + networkServiceRecord.getName());
    monitoringManager.stopMonitorNS(networkServiceRecord);
    highAvailabilityManager.stopConfigureRedundancy(networkServiceRecord.getId());
    ManagedNetworkServiceRecord mnsr = mnsrRepo.findByNsrId(networkServiceRecord.getId());
    if (mnsr != null) {
      for (String unSubscriptionId : mnsr.getUnSubscriptionIds())
        try {
          eventSubscriptionManger.unSubscribe(unSubscriptionId);
        } catch (SDKException | ClassNotFoundException | FileNotFoundException e) {
          throw new MonitoringException(e.getMessage(), e);
        }
      mnsrRepo.delete(mnsr);
    }
    log.debug("Unmanaged nsr:" + networkServiceRecord.getName());
  }

  @Override
  public boolean isNSRManaged(String id) {
    return mnsrRepo.findByNsrId(id) != null;
  }

  @Override
  public void cleanUnmanagedNsrs() throws FileNotFoundException, SDKException {
    Set<String> mnsrToRemove = new HashSet<>();
    for (ManagedNetworkServiceRecord mnsr : mnsrRepo.findAll()) {
      if (!nfvoRequestorWrapper.nsrExists(mnsr.getNsrId())) mnsrToRemove.add(mnsr.getId());
    }
    for (String mnsrIdToRemove : mnsrToRemove) {
      log.debug("Cleaning mnsr for not existing nsr");
      mnsrRepo.delete(mnsrIdToRemove);
    }
  }

  @Override
  public boolean isAVNFAlarm(String id) {
    return monitoringManager.isVNFThreshold(id);
  }

  @Override
  public FaultManagementPolicy getVNFFaultManagementPolicy(String vnfFMPolicyId)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    for (NetworkServiceRecord nsr : nfvoRequestorWrapper.getNsrs()) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu())
          for (FaultManagementPolicy vnffmp : vdu.getFault_management_policy()) {
            if (vnffmp != null && vnffmp.getId().equals(vnfFMPolicyId)) return vnffmp;
          }
      }
    }
    return null;
  }

  public String getVnfrIdByPolicyId(String policyId)
      throws SDKException, ClassNotFoundException, FileNotFoundException {
    for (NetworkServiceRecord nsr : nfvoRequestorWrapper.getNsrs()) {
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
        for (VirtualDeploymentUnit vdu : vnfr.getVdu())
          for (FaultManagementPolicy vnffmp : vdu.getFault_management_policy()) {
            if (vnffmp != null && vnffmp.getId().equals(policyId)) return vnfr.getId();
          }
      }
    }
    return null;
  }

  @Override
  public String getPolicyIdByThresholdId(String triggerId) {
    return monitoringManager.getPolicyIdFromTrhresholdId(triggerId);
  }
}
