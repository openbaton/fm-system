package org.openbaton.faultmanagement.fc.policymanagement.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;

/**
 * Created by mob on 30.10.15.
 */
public interface PolicyManager {
    void manageNSR(NetworkServiceRecord networkServiceRecord) throws FaultManagementPolicyException, HighAvailabilityException;
    void unManageNSR(NetworkServiceRecord networkServiceRecord) throws MonitoringException;
    boolean isNSRManaged(String id);
    boolean isVNFAlarm(String triggerId);
    VRFaultManagementPolicy getVNFFaultManagementPolicy(String vnfFMPolicyId);
    VirtualNetworkFunctionRecordShort getVNFRShort(String vnfFMPolicyId);
    String getPolicyIdByThresholdId(String triggerId);
    String getVnfrIdByPolicyId(String policyId);
}
