package org.openbaton.faultmanagement.repo;

import org.openbaton.catalogue.nfvo.VNFCDependencyParameters;
import org.openbaton.faultmanagement.catalogue.ManagedNetworkServiceRecord;
import org.openbaton.faultmanagement.catalogue.ThresholdHostnames;
import org.openbaton.faultmanagement.catalogue.VduPmJobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 28/06/16.
 */
@Transactional(readOnly = true)
public class ManagedNetworkServiceRecordRepositoryImpl
    implements ManagedNetworkServiceRecordRepositoryCustom {

  @Autowired ManagedNetworkServiceRecordRepository mnsrRepo;
  @Autowired VduPmJobsRepository vduPmJobsRepository;
  @Autowired ThresholdHostnamesRepository thresholdHostnamesRepository;

  @Override
  @Transactional
  public ManagedNetworkServiceRecord addPmJobId(String nsrId, String vduId, String pmJobId) {
    ManagedNetworkServiceRecord mnsr = mnsrRepo.findByNsrId(nsrId);
    if (mnsr != null) {
      if (mnsr.getVduIdPmJobIdMap().get(vduId) == null) {
        List<String> pmjobIds = new ArrayList<>();
        pmjobIds.add(pmJobId);
        VduPmJobs vduPmJobs = new VduPmJobs();
        vduPmJobs.setPmJobsIds(pmjobIds);
        vduPmJobsRepository.save(vduPmJobs);
        mnsr.getVduIdPmJobIdMap().put(vduId, vduPmJobs);
      } else {
        VduPmJobs vduPmJobs = mnsr.getVduIdPmJobIdMap().get(vduId);
        vduPmJobs.getPmJobsIds().add(pmJobId);
        vduPmJobsRepository.save(vduPmJobs);
        mnsr.getVduIdPmJobIdMap().put(vduId, vduPmJobs);
      }
    }
    return mnsr;
  }

  @Override
  @Transactional
  public ManagedNetworkServiceRecord addThresholdHostnames(
      String nsrId, String thresholdId, ThresholdHostnames thresholdHostnames) {
    ManagedNetworkServiceRecord mnsr = mnsrRepo.findByNsrId(nsrId);
    if (mnsr != null) {
      thresholdHostnamesRepository.save(thresholdHostnames);
      mnsr.getHostnames().put(thresholdId, thresholdHostnames);
    }
    return mnsr;
  }

  @Override
  @Transactional
  public ManagedNetworkServiceRecord addFmPolicyId(
      String nsrId, String thresholdId, String fmPolicyId) {
    ManagedNetworkServiceRecord mnsr = mnsrRepo.findByNsrId(nsrId);
    if (mnsr != null) {
      mnsr.getThresholdIdFmPolicyMap().put(thresholdId, fmPolicyId);
    }
    return mnsr;
  }

  @Override
  @Transactional
  public ManagedNetworkServiceRecord addVnfTriggerId(String nsrId, String thresholdId) {
    ManagedNetworkServiceRecord mnsr = mnsrRepo.findByNsrId(nsrId);
    if (mnsr != null) {
      mnsr.getVnfTriggerId().add(thresholdId);
    }
    return mnsr;
  }
}
