package org.openbaton.faultmanagement.fc.policymanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.NSFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.NetworkServiceRecordShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualDeploymentUnitShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.VnfFaultMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by mob on 29.10.15.
 */
@Service
public class PolicyManagerImpl implements PolicyManager {

    private static final Logger log = LoggerFactory.getLogger(PolicyManagerImpl.class);
    private List<NetworkServiceRecordShort> networkServiceRecordShortList;
    @Autowired
    VnfFaultMonitor vnfFaultMonitor;
    @PostConstruct
    public void init(){
        networkServiceRecordShortList=new ArrayList<>();
    }

    @Override
    public void manageNSR(NetworkServiceRecord nsr) throws FaultManagementPolicyException {
        if(!nsrNeedsMonitoring(nsr)){
            log.debug("The NSR"+ nsr.getName()+" needn't fault management monitoring");
            return;
        }
        log.debug("The NSR"+ nsr.getName()+" need fault management monitoring");
        //NetworkServiceRecordShort nsrs= getNSRShort(nsr);
        for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
            vnfFaultMonitor.startMonitorVNF(vnfr);
        }
    }

    private boolean nsrNeedsMonitoring(NetworkServiceRecord nsr) {
        if(nsr.getFaultManagementPolicy() != null && !nsr.getFaultManagementPolicy().isEmpty())
            return true;
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
            if(vnfr.getFault_management_policy()!= null && !vnfr.getFault_management_policy().isEmpty())
                return true;
        }
        return false;
    }

    private NetworkServiceRecordShort getNSRShort(NetworkServiceRecord nsr) throws FaultManagementPolicyException {

        NetworkServiceRecordShort nsrs = new NetworkServiceRecordShort(nsr.getId(),nsr.getName());
        Set<? extends FaultManagementPolicy> fmpolicies=nsr.getFaultManagementPolicy();
        if(fmpolicies.isEmpty())
            log.warn("No NS fault management policies found for the NS: "+nsr.getName()+" with id: "+nsr.getId());
        else{
            log.debug("Found the following NS fault management policies: "+nsr.getFaultManagementPolicy());
            for(FaultManagementPolicy fmp: fmpolicies){
                if(!(fmp instanceof NSFaultManagementPolicy))
                    throw new FaultManagementPolicyException("Impossible to cast to NSFaultManagementPolicy");
                NSFaultManagementPolicy nsFMPolicy= (NSFaultManagementPolicy) fmp;
                nsrs.addNsFaultManagementPolicy(nsFMPolicy);
            }
        }
        for(VirtualNetworkFunctionRecord vnfr: nsr.getVnfr()){
            fmpolicies.clear();
            fmpolicies = vnfr.getFault_management_policy();
            VirtualNetworkFunctionRecordShort vnfrs=new VirtualNetworkFunctionRecordShort(vnfr.getId(),vnfr.getName(),vnfr.getParent_ns_id());
            if(fmpolicies==null || fmpolicies.isEmpty())
                log.warn("No VNF fault management policies found for the VNF: "+vnfr.getName()+" with id: "+vnfr.getId());
            else{
                log.debug("Found the following VNF fault management policies: "+vnfr.getFault_management_policy());
                for(FaultManagementPolicy fmp: fmpolicies){
                    if(!(fmp instanceof VNFFaultManagementPolicy))
                        throw new FaultManagementPolicyException("Impossible to cast to VNFFaultManagementPolicy");
                    VNFFaultManagementPolicy vnfFMPolicy= (VNFFaultManagementPolicy) fmp;
                    vnfrs.addVnfFaultManagementPolicy(vnfFMPolicy);
                }
            }
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                VirtualDeploymentUnitShort vdus= new VirtualDeploymentUnitShort(vdu.getId(),vdu.getName());
                vdus.setMonitoringParameters(vdu.getMonitoring_parameter());

                log.debug("Created vdus of vnfd:"+vnfr.getName());
                vnfrs.addVirtualDeploymentUnitShort(vdus);
            }
            nsrs.addVNFS(vnfrs);
        }
        this.networkServiceRecordShortList.add(nsrs);
        return nsrs;
    }

    @Override
    public void unManageNSR(String id) {
        for (NetworkServiceRecordShort nsrs : networkServiceRecordShortList) {
            if (nsrs.getId().equals(id)) {
                for(VirtualNetworkFunctionRecordShort vnfrs: nsrs.getVirtualNetworkFunctionRecordShorts()){
                    //faultMonitor.stopMonitorVNF(vnfrs);
                }
                networkServiceRecordShortList.remove(nsrs);
                break;
            }
        }
    }
}
