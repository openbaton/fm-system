package org.openbaton.faultmanagement.repo;

import org.openbaton.faultmanagement.catalogue.ManagedNetworkServiceRecord;
import org.openbaton.faultmanagement.catalogue.ThresholdHostnames;

/**
 * Created by mob on 28/06/16.
 */
public interface ManagedNetworkServiceRecordRepositoryCustom {
  ManagedNetworkServiceRecord addPmJobId(String nsrId, String vduId, String pmJobId);

  ManagedNetworkServiceRecord addThresholdHostnames(
      String nsrId, String thresholdId, ThresholdHostnames thresholdHostnames);

  ManagedNetworkServiceRecord addFmPolicyId(String nsrId, String thresholdId, String fmPolicyId);

  ManagedNetworkServiceRecord addVnfTriggerId(String nsrId, String thresholdId);
}
