package org.openbaton.faultmanagement.managers;

import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mob on 29.10.15.
 */
public class VirtualNetworkFunctionRecordShort {
    private String id;
    private String name;
    private String nsrFatherId;
    private List<VNFFaultManagementPolicy> vnfFaultManagementPolicies;
    private List<VirtualDeploymentUnitShort> virtualDeploymentUnitShorts;

    public VirtualNetworkFunctionRecordShort(String id, String name,String nsrFatherId){
        this.id=id;
        this.name=name;
        this.nsrFatherId=nsrFatherId;
        vnfFaultManagementPolicies = new ArrayList<>();
        virtualDeploymentUnitShorts = new ArrayList<>();
    }

    public String getNsrFatherId() {
        return nsrFatherId;
    }

    public void setNsrFatherId(String nsrFatherId) {
        this.nsrFatherId = nsrFatherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public List<VNFFaultManagementPolicy> getVnfFaultManagementPolicies() {
        return vnfFaultManagementPolicies;
    }

    public void setVnfFaultManagementPolicies(List<VNFFaultManagementPolicy> vnfFaultManagementPolicies) {
        this.vnfFaultManagementPolicies = vnfFaultManagementPolicies;
    }

    public void addVnfFaultManagementPolicy(VNFFaultManagementPolicy vnfFaultManagementPolicy){
        if(vnfFaultManagementPolicy==null)
            throw new NullPointerException("vnf fault management policy is null");
        this.vnfFaultManagementPolicies.add(vnfFaultManagementPolicy);
    }

    public List<VirtualDeploymentUnitShort> getVirtualDeploymentUnitShorts() {
        return virtualDeploymentUnitShorts;
    }

    public void addVirtualDeploymentUnitShort(VirtualDeploymentUnitShort vdus){
        if(vdus==null)
            throw new NullPointerException("vdu short is null");
        virtualDeploymentUnitShorts.add(vdus);
    }

    @Override
    public String toString() {
        return "VirtualNetworkFunctionRecordShort{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nsrFatherId='" + nsrFatherId + '\'' +
                ", vnfFaultManagementPolicies=" + vnfFaultManagementPolicies +
                ", virtualDeploymentUnitShorts=" + virtualDeploymentUnitShorts +
                '}';
    }
}