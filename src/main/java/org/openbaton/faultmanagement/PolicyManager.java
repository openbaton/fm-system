package org.openbaton.faultmanagement;
import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.*;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.exceptions.FaultManagementPolicyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by mob on 29.10.15.
 */
@Service
public class PolicyManager implements PolicyManagerInterface{

    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    private List<NetworkServiceRecordShort> networkServiceRecordShortList;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private FaultMonitor faultMonitor;

    @PostConstruct
    public void init(){
        networkServiceRecordShortList=new ArrayList<>();
    }

    public void manageNSR(NetworkServiceRecord nsr) throws FaultManagementPolicyException {
        checkNsr(nsr);

        for(NetworkServiceRecordShort networkServiceRecordShort : networkServiceRecordShortList){
            for(VirtualNetworkFunctionRecordShort vnfs : networkServiceRecordShort.getVirtualNetworkFunctionRecordShorts()){
                for(VNFFaultManagementPolicy vnfp: vnfs.getVnfFaultManagementPolicies()){
                    //List<String> vduList
                }
            }
        }
    }

    private void checkNsr(NetworkServiceRecord nsr) throws FaultManagementPolicyException {
        if(nsr == null)
            throw new NullPointerException("The nsr is null");
        //TODO Check if the nsr must be monitored
        NetworkServiceRecordShort nsrs = new NetworkServiceRecordShort(nsr.getId(),nsr.getName());
        Set<FaultManagementPolicy> fmpolicies=nsr.getFaultManagementPolicy();
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
            fmpolicies= vnfr.getFaultManagementPolicy();
            VirtualNetworkFunctionRecordShort vnfrs=new VirtualNetworkFunctionRecordShort(vnfr.getId(),vnfr.getName(),vnfr.getParent_ns_id());
            if(fmpolicies.isEmpty())
                log.warn("No VNF fault management policies found for the VNF: "+vnfr.getName()+" with id: "+vnfr.getId());
            else{
                log.debug("Found the following VNF fault management policies: "+vnfr.getFaultManagementPolicy());
                for(FaultManagementPolicy fmp: fmpolicies){
                    if(!(fmp instanceof VNFFaultManagementPolicy))
                        throw new FaultManagementPolicyException("Impossible to cast to VNFFaultManagementPolicy");
                    VNFFaultManagementPolicy vnfFMPolicy= (VNFFaultManagementPolicy) fmp;
                    vnfrs.addVnfFaultManagementPolicy(vnfFMPolicy);
                }
            }
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                VirtualDeploymentUnitShort vdus= new VirtualDeploymentUnitShort(vdu.getId(),vdu.getName());
                vnfrs.addVirtualDeploymentUnitShort(vdus);
            }
            nsrs.addVNFS(vnfrs);
        }
        this.networkServiceRecordShortList.add(nsrs);
    }
    public void unManageNSR(String id) {
        for (NetworkServiceRecordShort nsrs : networkServiceRecordShortList) {
            if (nsrs.getId().equals(id)) {
                networkServiceRecordShortList.remove(nsrs);
                break;
            }
        }
    }
}
