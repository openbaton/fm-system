package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.faultmanagement.exceptions.FaultManagementPolicyException;

/**
 * Created by mob on 30.10.15.
 */
public interface PolicyManagerInterface {
    void manageNSR(NetworkServiceRecord networkServiceRecord) throws FaultManagementPolicyException;
    void unManageNSR(String nsrId);
}
