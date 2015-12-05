package org.openbaton.faultmanagement.fc.policymanagement.catalogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mob on 30.10.15.
 */
public class VirtualDeploymentUnitShort {
    private String id;
    private String name;
    private Set<String> monitoringParameters;
    private List<VNFCInstanceShort> vnfcInstanceShortList;

    public VirtualDeploymentUnitShort(String id, String name){
        if(id==null)
            throw new NullPointerException("VDUshort id cannot be null");
        this.id=id;
        this.name=name;
        this.monitoringParameters=new HashSet<>();
        vnfcInstanceShortList =new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

/*
    public Set<MonitoringParameter> getMonitoringParameters() {
        return monitoringParameters;
    }
*/
    public void addVNFCInstanceShort (VNFCInstanceShort vnfcInstanceShort){
        vnfcInstanceShortList.add(vnfcInstanceShort);
    }

    public List<VNFCInstanceShort> getVnfcInstanceShortList() {
        return vnfcInstanceShortList;
    }

    public void setVnfcInstanceShortList(List<VNFCInstanceShort> vnfcInstanceShortList) {
        this.vnfcInstanceShortList = vnfcInstanceShortList;
    }

    public String getName() {
        return name;
    }

    public void setMonitoringParameters(Set<String> monitoringParameters) {
        this.monitoringParameters = monitoringParameters;
    }

    /*public boolean isMetricPresent(Metric metric){
        for (String mp: monitoringParameters){
            if(mp.getMetric().ordinal()==metric.ordinal())
                return true;
        }
        return false;
    }
    public MonitoringParameter getMonitoringParameter(Metric metric){
        if(isMetricPresent(metric)){
            for (String mp: monitoringParameters){
                if(mp.getMetric().ordinal()==metric.ordinal())
                    return mp;
            }
        }
        return null;
    }*/
    public List<String> getVNFCInstanceIdFromHostname(List<String> hostnames){
        List<String> result=new ArrayList<>();
        for(VNFCInstanceShort vnfcInstanceShort : vnfcInstanceShortList){
            if(hostnames.contains(vnfcInstanceShort.getHostname()))
                result.add(vnfcInstanceShort.getId());
        }
        return result;
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
