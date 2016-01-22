package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

import java.util.List;

/**
 * Created by mob on 11.01.16.
 */
public interface NSRManager {
    NetworkServiceRecord getNetworkServiceRecord(String nsrId);
    List<NetworkServiceRecord> getNetworkServiceRecords();
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId);
    VNFCInstance getVNFCInstance(String hostname);
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId);
    VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr,String vnfcInstaceId);
    VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId);
}
