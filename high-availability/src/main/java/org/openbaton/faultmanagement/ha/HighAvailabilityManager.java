package org.openbaton.faultmanagement.ha;

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;

/**
 * Created by mob on 11.01.16.
 */
public interface HighAvailabilityManager {
    void switchToRedundantVNFC(String nsrId,String vnfrId, String vduId, String vnfcInstanceId) throws HighAvailabilityException;
    void configureRedundancy(VirtualNetworkFunctionRecord nsr) throws HighAvailabilityException;
    void createStandByVNFC(VNFComponent vnfComponent, VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu) throws HighAvailabilityException;
    void switchToRedundantVNFC(VirtualNetworkFunctionRecord vnfr,VirtualDeploymentUnit vdu)throws HighAvailabilityException;
}
