package org.openbaton.faultmanagement.fc.interfaces;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

import java.util.List;

/**
 * Created by mob on 11.01.16.
 */
public interface NSRManager {
    NetworkServiceRecord getNetworkServiceRecord(String nsrId);
    List<NetworkServiceRecord> getNetworkServiceRecords();
    VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(String nsrId,String vnfrId);
}
