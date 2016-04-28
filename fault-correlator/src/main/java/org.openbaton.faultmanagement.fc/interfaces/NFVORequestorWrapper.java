package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;

import java.util.List;

/**
 * Created by mob on 11.01.16.
 */
public interface NFVORequestorWrapper {
    NetworkServiceRecord getNetworkServiceRecord(String nsrId) throws NotFoundException, NFVORequestorException;
    List<NetworkServiceRecord> getNetworkServiceRecords() throws NFVORequestorException;
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId) throws NotFoundException, NFVORequestorException;
    VNFCInstance getVNFCInstance(String hostname) throws NFVORequestorException;
    VNFCInstance getVNFCInstanceById(String VnfcId) throws NFVORequestorException;
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String vnfrId) throws NFVORequestorException;
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecordFromVNFCHostname(String hostname) throws NFVORequestorException;
    VNFCInstance getVNFCInstanceFromVnfr(VirtualNetworkFunctionRecord vnfr,String vnfcInstaceId);
    VirtualDeploymentUnit getVDU(VirtualNetworkFunctionRecord vnfr, String vnfcInstaceId);
}
