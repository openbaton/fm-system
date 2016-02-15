package org.openbaton.faultmanagement.ha;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;

/**
 * Created by mob on 11.01.16.
 */
public interface HighAvailabilityManager {
    void switchToRedundantVNFC(VNFCInstance failedVnfcInstance,String nsrId,String vnfrId, String vduId, String vnfcInstanceId) throws HighAvailabilityException;
    void configureRedundancy(VirtualNetworkFunctionRecord nsr) throws HighAvailabilityException, UnirestException;
    void createStandByVNFC(VNFComponent vnfComponent, VirtualNetworkFunctionRecord vnfr, VirtualDeploymentUnit vdu) throws HighAvailabilityException;
    void switchToRedundantVNFC(VNFCInstance failedVnfcInstance, VirtualNetworkFunctionRecord vnfr,VirtualDeploymentUnit vdu)throws HighAvailabilityException;
    void executeHeal(String cause, String nsrId, String vnfrId, String vduId, String vnfcInstanceId);
    String cleanFailedInstances(VirtualNetworkFunctionRecord vnfr) throws UnirestException;
}
