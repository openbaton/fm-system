/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

import org.openbaton.catalogue.mano.common.faultmanagement.NSFaultManagementPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 29.10.15.
 */
public class NetworkServiceRecordShort {
    private String id;
    private String name;
    private List<NSFaultManagementPolicy> nsFaultManagementPolicies;
    private List<VirtualNetworkFunctionRecordShort> virtualNetworkFunctionRecordShorts;

    public NetworkServiceRecordShort(String id, String name){
        this.id=id;
        this.name=name;
        this.nsFaultManagementPolicies=new ArrayList<>();
        virtualNetworkFunctionRecordShorts=new ArrayList<>();

    }

    public List<NSFaultManagementPolicy> getNsFaultManagementPolicies() {
        return nsFaultManagementPolicies;
    }

    public void setNsFaultManagementPolicies(List<NSFaultManagementPolicy> nsFaultManagementPolicies) {
        this.nsFaultManagementPolicies = nsFaultManagementPolicies;
    }

    public void addNsFaultManagementPolicy(NSFaultManagementPolicy nsFaultManagementPolicy) {
        if(nsFaultManagementPolicy==null)
            throw new NullPointerException("ns fault management policy is null");
        this.nsFaultManagementPolicies.add(nsFaultManagementPolicy);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public List<VirtualNetworkFunctionRecordShort> getVirtualNetworkFunctionRecordShorts() {
        return virtualNetworkFunctionRecordShorts;
    }
    public void addVNFS(VirtualNetworkFunctionRecordShort virtualNetworkFunctionRecordShort) {
        if(virtualNetworkFunctionRecordShort==null)
            throw new NullPointerException("virtualNetworkFunctionRecordShort fault management policy is null");
        this.virtualNetworkFunctionRecordShorts.add(virtualNetworkFunctionRecordShort);
    }
}
