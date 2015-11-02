package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.NSFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.interfaces.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by mob on 29.10.15.
 */
@Service
@Scope
public class PolicyManagerImpl implements PolicyManager {

    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    private List<NetworkServiceRecordShort> networkServiceRecordShortList;
    private final ScheduledExecutorService vnfScheduler = Executors.newScheduledThreadPool(1);
    private Map<String,ScheduledFuture<?>> futures;
    private final ScheduledExecutorService nsScheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init(){
        futures=new HashMap<>();
        networkServiceRecordShortList=new ArrayList<>();
    }

    @Override
    public void manageNSR(NetworkServiceRecord nsr) throws FaultManagementPolicyException {
        if(!nsrNeedsMonitoring(nsr)){
            log.debug("The NSR"+ nsr.getName()+" needn't fault management monitoring");
            return;
        }
        NetworkServiceRecordShort nsrs= getNSRShort(nsr);
        for(VirtualNetworkFunctionRecordShort vnfs : nsrs.getVirtualNetworkFunctionRecordShorts()){
            for(VNFFaultManagementPolicy vnfp: vnfs.getVnfFaultManagementPolicies()){
                VNFFaultMonitor fm = new VNFFaultMonitor(vnfp,vnfs.getVirtualDeploymentUnitShorts().get(0));
                futures.put(vnfp.getName(),vnfScheduler.scheduleAtFixedRate(fm, 1, vnfp.getPeriod(), TimeUnit.SECONDS));
            }
        }

    }

    private boolean nsrNeedsMonitoring(NetworkServiceRecord nsr) {
        if(nsr == null)
            throw new NullPointerException("The nsr is null");
        //TODO Check if the nsr must be monitored
        return true;
    }

    private NetworkServiceRecordShort getNSRShort(NetworkServiceRecord nsr) throws FaultManagementPolicyException {

        NetworkServiceRecordShort nsrs = new NetworkServiceRecordShort(nsr.getId(),nsr.getName());
        log.debug("here!");
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
            fmpolicies = vnfr.getFaultManagementPolicy();
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
                vdus.setMonitoringParameters(vdu.getMonitoring_parameter());
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
                    for(VNFFaultManagementPolicy vnfp: vnfrs.getVnfFaultManagementPolicies()){
                        if(futures.get(vnfp.getName())!=null)
                            futures.get(vnfp.getName()).cancel(true);
                    }
                }
                networkServiceRecordShortList.remove(nsrs);
                break;
            }
        }
    }
}
