package org.openbaton.faultmanagement.managers;

/**
 * Created by mob on 04.11.15.
 */
public interface FaultMonitor {
    void startMonitorVNF(VirtualNetworkFunctionRecordShort vnfs);
    void stopMonitorVNF(VirtualNetworkFunctionRecordShort vnfs);
}
