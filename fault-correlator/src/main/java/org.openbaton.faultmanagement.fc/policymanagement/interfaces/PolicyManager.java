package org.openbaton.faultmanagement.fc.policymanagement.interfaces;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;

/**
 * Created by mob on 30.10.15.
 */
public interface PolicyManager {
    void manageNSR(NetworkServiceRecord networkServiceRecord) throws FaultManagementPolicyException;
    void unManageNSR(String nsrId);
}
