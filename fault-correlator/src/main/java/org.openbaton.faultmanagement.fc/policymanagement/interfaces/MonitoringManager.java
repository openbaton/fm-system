package org.openbaton.faultmanagement.fc.policymanagement.interfaces;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.exceptions.MonitoringException;

import java.util.List;

/**
 * Created by mob on 04.11.15.
 */
public interface MonitoringManager {
    void startMonitorNS(NetworkServiceRecord networkServiceRecord);
    void stopMonitorNS(NetworkServiceRecord nsr) throws MonitoringException;
    List<String> getHostnamesFromTrhresholdId(String thresholdId);
    String getPolicyIdFromTrhresholdId(String thresholdId);
}
