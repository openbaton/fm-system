package org.openbaton.faultmanagement.fc.policymanagement.interfaces;

import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;

/**
 * Created by mob on 30.10.15.
 */
public interface PolicyManager {
    void manageNSR(NetworkServiceRecord networkServiceRecord) throws FaultManagementPolicyException;
    void unManageNSR(NetworkServiceRecord networkServiceRecord) throws MonitoringException;
    boolean isNSRManaged(String id);
    VNFFaultManagementPolicy getVNFFaultManagementPolicy(String vnfFMPolicyId);
    VirtualNetworkFunctionRecordShort getVNFRShort(String vnfFMPolicyId);
}
