package org.openbaton.faultmanagement.managers;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;

/**
 * Created by mob on 04.11.15.
 */
public interface VnfFaultMonitor {
    void startMonitorVNF(VirtualNetworkFunctionRecord vnfr);
    void stopMonitorVNF(VirtualNetworkFunctionRecord vnfr);
}
