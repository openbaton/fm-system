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
