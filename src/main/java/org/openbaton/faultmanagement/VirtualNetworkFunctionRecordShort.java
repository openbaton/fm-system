package org.openbaton.faultmanagement;

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

    public VirtualNetworkFunctionRecordShort(String id, String name,String nsrFatherId){
        this.id=id;
        this.name=name;
        this.nsrFatherId=nsrFatherId;
        vnfFaultManagementPolicies = new ArrayList<>();
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

}
