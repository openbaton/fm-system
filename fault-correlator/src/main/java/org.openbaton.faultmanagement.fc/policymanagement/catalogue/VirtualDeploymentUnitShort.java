/*
* Copyright (c) 2015-2016 Fraunhofer FOKUS
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
