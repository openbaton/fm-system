package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NotFoundException;

import java.util.List;

/**
 * Created by mob on 11.01.16.
 */
public interface NSRManager {
    NetworkServiceRecord getNetworkServiceRecord(String nsrId) throws NotFoundException;
    List<NetworkServiceRecord> getNetworkServiceRecords();
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) throws NotFoundException;
    VNFCInstance getVNFCInstance(String hostname);
    VNFCInstance getVNFCInstanceById(String VnfcId);
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId);
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname);
    VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr,String vnfcInstaceId);
    VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId);
}
