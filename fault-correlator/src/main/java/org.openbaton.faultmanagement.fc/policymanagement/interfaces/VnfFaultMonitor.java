package org.openbaton.faultmanagement.fc.policymanagement.interfaces;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

/**
 * Created by mob on 04.11.15.
 */
public interface VnfFaultMonitor {
    void startMonitorVNF(VirtualNetworkFunctionRecord vnfrs);
    void stopMonitorVNF(VirtualNetworkFunctionRecord vnfrs);
}
