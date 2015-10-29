package org.openbaton.faultmanagement;

import java.util.List;

/**
 * Created by mob on 29.10.15.
 */
public class FaultManagementPolicy {
    private List<MonitoredParameter> monitoredParameters;
    private String policyName;
    private String policyType;

    public FaultManagementPolicy(String policyName, String policyType){
        this.policyName=policyName;
        this.policyType=policyType;
    }

    public List<MonitoredParameter> getMonitoredParameters() {
        return monitoredParameters;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void addMonitoredParameter(MonitoredParameter monitoredParameter){
        if(monitoredParameter==null)
            throw new NullPointerException("The monitoredParameter is null");
        this.monitoredParameters.add(monitoredParameter);
    }
}
