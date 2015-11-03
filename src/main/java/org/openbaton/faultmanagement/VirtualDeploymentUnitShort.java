package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.Metric;
import org.openbaton.catalogue.mano.common.faultmanagement.MonitoringParameter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mob on 30.10.15.
 */
public class VirtualDeploymentUnitShort {
    private String id;
    private String name;
    private Set<MonitoringParameter> monitoringParameters;

    public VirtualDeploymentUnitShort(String id, String name){
        if(id==null || name==null)
            throw new NullPointerException("VDUshort name or id cannot be null");
        this.id=id;
        this.name=name;
        this.monitoringParameters=new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<MonitoringParameter> getMonitoringParameters() {
        return monitoringParameters;
    }

    public String getName() {
        return name;
    }

    public void setMonitoringParameters(Set<MonitoringParameter> monitoringParameters) {
        this.monitoringParameters = monitoringParameters;
    }

    public boolean isMetricPresent(Metric metric){
        for (MonitoringParameter mp: monitoringParameters){
            if(mp.getMetric().ordinal()==metric.ordinal())
                return true;
        }
        return false;
    }
    public MonitoringParameter getMonitoringParameter(Metric metric){
        if(isMetricPresent(metric)){
            for (MonitoringParameter mp: monitoringParameters){
                if(mp.getMetric().ordinal()==metric.ordinal())
                    return mp;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "VirtualDeploymentUnitShort{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", monitoringParameters=" + monitoringParameters +
                '}';
    }
}
