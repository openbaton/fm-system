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

package org.openbaton.faultmanagement.core.ham;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.openbaton.catalogue.mano.common.ResiliencyLevel;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.core.ham.exceptions.HighAvailabilityException;
import org.openbaton.faultmanagement.core.ham.interfaces.HighAvailabilityManager;
import org.openbaton.faultmanagement.requestor.interfaces.NFVORequestorWrapper;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/** Created by mob on 11.01.16. */
@Service
@ConfigurationProperties
public class HighAvailabilityManagerImpl implements HighAvailabilityManager {

  @Autowired private NFVORequestorWrapper nfvoRequestorWrapper;
  private static final Logger log = LoggerFactory.getLogger(HighAvailabilityManagerImpl.class);
  private Map<String, ScheduledFuture<?>> futures = new HashMap<>();
  private final ScheduledExecutorService nsScheduler = Executors.newScheduledThreadPool(1);

  @Value("${fms.redundancycheck:60}")
  private String redundancyCheck;

  public void configureRedundancy(NetworkServiceRecord nsr) throws HighAvailabilityException {
    HighAvailabilityConfigurator highAvailabilityConfigurator =
        new HighAvailabilityConfigurator(nsr);
    int interval = Integer.parseInt(redundancyCheck.trim());
    log.debug("Starting HighAvailabilityConfigurator thread every: " + interval + " seconds");
    futures.put(
        nsr.getId(),
        nsScheduler.scheduleAtFixedRate(
            highAvailabilityConfigurator, 5, interval, TimeUnit.SECONDS));
  }

  private VNFComponent getVNFComponent(VirtualDeploymentUnit vdu) {

    VNFComponent componentSample = vdu.getVnfc().iterator().next();

    VNFComponent vnfComponent_new = new VNFComponent();
    Set<VNFDConnectionPoint> vnfdConnectionPointSet = new HashSet<>();
    for (VNFDConnectionPoint vnfdConnectionPointSample : componentSample.getConnection_point()) {
      VNFDConnectionPoint vnfdConnectionPoint = new VNFDConnectionPoint();
      vnfdConnectionPoint.setVirtual_link_reference(
          vnfdConnectionPointSample.getVirtual_link_reference());
      vnfdConnectionPoint.setFloatingIp(vnfdConnectionPointSample.getFloatingIp());
      vnfdConnectionPoint.setType(vnfdConnectionPointSample.getType());

      vnfdConnectionPointSet.add(vnfdConnectionPoint);
    }

    vnfComponent_new.setConnection_point(vnfdConnectionPointSet);

    return vnfComponent_new;
  }

  public void cleanFailedInstances(String nsrId) throws HighAvailabilityException {
    try {
      NetworkServiceRecord nsr = nfvoRequestorWrapper.getNsr(nsrId);
      for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr())
        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
          for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
            if (vnfcInstance.getState() != null
                && vnfcInstance.getState().equalsIgnoreCase("failed")) {
              log.info(
                  "The vnfcInstance: "
                      + vnfcInstance.getHostname()
                      + " of the vnfr: "
                      + vnfr.getName()
                      + " is in "
                      + vnfcInstance.getState()
                      + " state");
              log.info("Deleting VNFCInstance:" + vnfcInstance.getHostname());
              nfvoRequestorWrapper.deleteVnfcInstance(
                  vnfr.getParent_ns_id(), vnfr.getId(), vdu.getId(), vnfcInstance.getId());
            }
          }
        }
    } catch (SDKException | ClassNotFoundException | FileNotFoundException e) {
      throw new HighAvailabilityException(e.getMessage(), e);
    }
  }

  public boolean hasFailedVnfcInstances(String vnfrId) throws HighAvailabilityException {
    try {
      VirtualNetworkFunctionRecord vnfr =
          nfvoRequestorWrapper.getVirtualNetworkFunctionRecord(vnfrId);
      for (VirtualDeploymentUnit vdu : vnfr.getVdu())
        for (VNFCInstance vnfcInstance : vdu.getVnfc_instance())
          if (vnfcInstance.getState() != null && vnfcInstance.getState().equalsIgnoreCase("failed"))
            return true;
    } catch (SDKException | ClassNotFoundException | FileNotFoundException e) {
      throw new HighAvailabilityException(e.getMessage(), e);
    }
    return false;
  }

  @Override
  public void stopConfigureRedundancy(String nsrId) {
    ScheduledFuture<?> future = futures.get(nsrId);
    if (future != null) future.cancel(true);
    futures.remove(nsrId);
  }

  private boolean checkMaxNumInstances(VirtualDeploymentUnit vdu) {
    if (vdu.getScale_in_out() == vdu.getVnfc_instance().size()) {
      log.warn(
          "The VirtualDeploymentUnit chosen has reached the maximum number of VNFCInstance. So, no VNFC in stanby can be created");
      return true;
    }
    return false;
  }

  private boolean checkIfStandbyVNFCInstance(VirtualDeploymentUnit vdu) {
    for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
      if (vnfcInstance.getState() != null && vnfcInstance.getState().equalsIgnoreCase("standby"))
        return true;
    }
    return false;
  }

  private boolean vnfrNeedsRedundancy(VirtualNetworkFunctionRecord vnfr) {

    for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
      if (vdu.getHigh_availability() != null
          && vdu.getHigh_availability().getRedundancyScheme() != null
          && !vdu.getHigh_availability().getRedundancyScheme().isEmpty()
          && vdu.getHigh_availability().getResiliencyLevel() != null) return true;
    }
    return false;
  }

  public void createStandByVNFC(
      VNFComponent vnfComponent,
      VirtualNetworkFunctionRecord vnfr,
      VirtualDeploymentUnit vdu,
      ArrayList<String> vimInstanceNames)
      throws HighAvailabilityException {
    try {
      nfvoRequestorWrapper.createStandbyVNFCInstance(
          vnfr.getParent_ns_id(), vnfr.getId(), vdu.getId(), vnfComponent, vimInstanceNames);
    } catch (SDKException | ClassNotFoundException | FileNotFoundException e) {
      throw new HighAvailabilityException(e.getMessage(), e);
    }
  }

  @Override
  public void switchToStandby(String vnfrId, String failedVnfcId) throws HighAvailabilityException {
    try {
      VirtualNetworkFunctionRecord vnfr =
          nfvoRequestorWrapper.getVirtualNetworkFunctionRecord(vnfrId);
      VNFCInstance failedVnfcInstance = nfvoRequestorWrapper.getVNFCInstanceById(failedVnfcId);
      VirtualDeploymentUnit vdu = findVduInVnfr(vnfr, failedVnfcId);
      VNFCInstance standbyVnfcInstance = findStandbyVnfcInstance(vdu);
      nfvoRequestorWrapper.switchToStandby(
          vnfr.getParent_ns_id(),
          vnfrId,
          vdu.getId(),
          standbyVnfcInstance.getId(),
          failedVnfcInstance);
    } catch (SDKException | ClassNotFoundException | FileNotFoundException e) {
      throw new HighAvailabilityException(e.getMessage(), e);
    }
  }

  @Override
  public void executeHeal(String failedVnfcInstanceId, String cause)
      throws HighAvailabilityException {
    try {
      VNFCInstance failedVnfcInstance =
          nfvoRequestorWrapper.getVNFCInstanceById(failedVnfcInstanceId);
      VirtualNetworkFunctionRecord vnfr =
          nfvoRequestorWrapper.getVirtualNetworkFunctionRecordFromVNFCHostname(
              failedVnfcInstance.getHostname());
      VirtualDeploymentUnit vdu = findVduInVnfr(vnfr, failedVnfcInstanceId);
      nfvoRequestorWrapper.executeHeal(
          vnfr.getParent_ns_id(), vnfr.getId(), vdu.getId(), failedVnfcInstanceId, cause);
    } catch (SDKException | ClassNotFoundException | FileNotFoundException e) {
      throw new HighAvailabilityException(e.getMessage(), e);
    }
  }

  private VirtualDeploymentUnit findVduInVnfr(
      VirtualNetworkFunctionRecord vnfr, String failedVnfcId) throws HighAvailabilityException {

    for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
      for (VNFCInstance vnfvInstance : vdu.getVnfc_instance()) {
        if (vnfvInstance.getId().equals(failedVnfcId)) return vdu;
      }
    }
    throw new HighAvailabilityException(
        "The vnfc with id:" + failedVnfcId + " is not in the vnfr: " + vnfr.getName());
  }

  private VNFCInstance findStandbyVnfcInstance(VirtualDeploymentUnit vdu)
      throws HighAvailabilityException {
    for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
      if (vnfcInstance.getState() != null && vnfcInstance.getState().equalsIgnoreCase("standby"))
        return vnfcInstance;
    }
    throw new HighAvailabilityException(
        "No vnfcinstance in standby found in the vdu with id:" + vdu.getId());
  }

  private class HighAvailabilityConfigurator implements Runnable {
    private NetworkServiceRecord nsr;

    public HighAvailabilityConfigurator(NetworkServiceRecord nsr) {
      this.nsr = nsr;
    }

    @Override
    public void run() {
      NetworkServiceRecord nsr = null;
      try {
        nsr = nfvoRequestorWrapper.getNsr(this.nsr.getId());
        if (nsr.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
          log.debug("Redundancy thread: the nsr to check redundancy is not in ACTIVE state");
          return;
        }
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
          if (!vnfrNeedsRedundancy(vnfr)) continue;
          for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
            if (vdu.getHigh_availability().getResiliencyLevel().ordinal()
                == ResiliencyLevel.ACTIVE_STANDBY_STATELESS.ordinal()) {
              if (vdu.getHigh_availability().getRedundancyScheme().equals("1:N")) {
                // check the 1:N redundancy
                if (checkIfStandbyVNFCInstance(vdu)) continue;
                if (checkMaxNumInstances(vdu)) continue;
                //Creating a new component to add into the vdu
                VNFComponent vnfComponent_new = getVNFComponent(vdu);

                log.info("Creating standby vnfc instance");
                log.debug("VNF component to send:" + vnfComponent_new);

                ArrayList<String> vimInstanceNames = new ArrayList<>();
                vimInstanceNames.addAll(vdu.getVimInstanceName());
                createStandByVNFC(
                    vnfComponent_new, vnfr, vdu, vimInstanceNames);
                log.debug("Creating standby vnfc instance message sent");
              }
            }
          }
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()) log.error(e.getMessage(), e);
        else log.error(e.getMessage());
      }
    }
  }
}
