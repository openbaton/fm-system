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

package org.openbaton.faultmanagement.fc.policymanagement;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.NSFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.fc.exceptions.NFVORequestorException;
import org.openbaton.faultmanagement.fc.interfaces.NFVORequestorWrapper;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.NetworkServiceRecordShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VNFCInstanceShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualDeploymentUnitShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.EventSubscriptionManger;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.MonitoringManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.ha.HighAvailabilityManager;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties
public class PolicyManagerImpl implements PolicyManager {

    private static final Logger log = LoggerFactory.getLogger(PolicyManagerImpl.class);
    private List<NetworkServiceRecordShort> networkServiceRecordShortList;
    private Map<String,ScheduledFuture<?>> futures;

    private final ScheduledExecutorService nsScheduler = Executors.newScheduledThreadPool(1);

    @Autowired private NFVORequestorWrapper NFVORequestorWrapper;

    @Autowired private MonitoringManager monitoringManager;

    @Autowired private EventSubscriptionManger eventSubscriptionManger;

    @Autowired private HighAvailabilityManager highAvailabilityManager;

    @Value("${fms.redundancycheck:60}")
    private String redundancyCheck;

    @PostConstruct
    public void init(){
        futures = new HashMap<>();
        networkServiceRecordShortList=new ArrayList<>();
    }

    @Override
    public void manageNSR(NetworkServiceRecord nsr) throws SDKException {
        if(!nsrNeedsMonitoring(nsr)){
            log.info("The NSR"+ nsr.getName()+" doesn't need fault management monitoring");
            return;
        }
        List<VirtualNetworkFunctionRecord> vnfrRequiringFaultManagement = getVnfrRequiringFaultManagement(nsr);
        log.debug("Those vnfr requires FM: "+vnfrRequiringFaultManagement);
        for(VirtualNetworkFunctionRecord vnfr : vnfrRequiringFaultManagement){
            eventSubscriptionManger.subscribe(vnfr, Action.HEAL);
        }
        log.debug("The NSR" + nsr.getName() + " needs fault management monitoring");
        NetworkServiceRecordShort nsrs = null;
        try {
            nsrs = getNSRShort(nsr);
        } catch (FaultManagementPolicyException e) {
            log.error("Getting the NSR short for the nsr: "+nsr.getName()+" "+e.getMessage(),e);
        }

        networkServiceRecordShortList.add(nsrs);

        monitoringManager.startMonitorNS(nsr);

        HighConfigurator highConfigurator = new HighConfigurator(nsr);
        int interval = Integer.parseInt(redundancyCheck.trim());
        log.debug("Starting redundancy thread every: "+interval+" seconds");
        futures.put(nsr.getId(),nsScheduler.scheduleAtFixedRate(highConfigurator,5,interval, TimeUnit.SECONDS));


    }

    private List<VirtualNetworkFunctionRecord> getVnfrRequiringFaultManagement(NetworkServiceRecord nsr) {
        List<VirtualNetworkFunctionRecord> result = new ArrayList<>();
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
            for(VirtualDeploymentUnit vdu : vnfr.getVdu())
                if(vdu.getFault_management_policy()!= null && !vdu.getFault_management_policy().isEmpty())
                    result.add(vnfr);
        }
        return result;
    }

    private boolean nsrNeedsMonitoring(NetworkServiceRecord nsr) {
        if(nsr.getFaultManagementPolicy() != null && !nsr.getFaultManagementPolicy().isEmpty())
            return true;
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
            for(VirtualDeploymentUnit vdu : vnfr.getVdu())
                if(vdu.getFault_management_policy()!= null && !vdu.getFault_management_policy().isEmpty())
                    return true;
        }
        return false;
    }
    private VirtualNetworkFunctionRecordShort getVNFRShortFromHostnames(List<String> hostnames){
        VirtualNetworkFunctionRecordShort result=null;

        return result;
    }
    @Override
    public boolean isAManagedAlarm(String triggerId){
        String policyId = monitoringManager.getPolicyIdFromTrhresholdId(triggerId);
        if(policyId==null)
            return false;
        return true;
    }
    private NetworkServiceRecordShort getNSRShort(NetworkServiceRecord nsr) throws FaultManagementPolicyException {

        NetworkServiceRecordShort nsrs = new NetworkServiceRecordShort(nsr.getId(),nsr.getName());
        Set<? extends FaultManagementPolicy> fmpolicies=nsr.getFaultManagementPolicy();
        if(fmpolicies.isEmpty()){
            //log.warn("No NS fault management policies found for the NS: "+nsr.getName()+" with id: "+nsr.getId());
        }

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

            VirtualNetworkFunctionRecordShort vnfrs=new VirtualNetworkFunctionRecordShort(vnfr.getId(),vnfr.getName(),vnfr.getParent_ns_id());

            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                fmpolicies = vdu.getFault_management_policy();
                VirtualDeploymentUnitShort vdus= new VirtualDeploymentUnitShort(vdu.getId(),vdu.getName());
                vdus.setMonitoringParameters(vdu.getMonitoring_parameter());
                if(fmpolicies==null || fmpolicies.isEmpty()){
                    //log.warn("No VR fault management policies found for the VNF: "+vnfr.getName()+" with id: "+vnfr.getId());
                }

                else{
                    log.debug("Found the following VR fault management policies: "+vdu.getFault_management_policy());
                    for(FaultManagementPolicy fmp: fmpolicies){
                        if(!(fmp instanceof VRFaultManagementPolicy))
                            throw new FaultManagementPolicyException("Impossible to cast to VRFaultManagementPolicy");
                        VRFaultManagementPolicy vnfFMPolicy= (VRFaultManagementPolicy) fmp;
                        vnfrs.addVnfFaultManagementPolicy(vnfFMPolicy);
                    }
                }
                for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                    VNFCInstanceShort vnfcInstanceShort = new VNFCInstanceShort(vnfcInstance.getId(),vnfcInstance.getHostname());
                    vdus.addVNFCInstanceShort(vnfcInstanceShort);
                }
                log.debug("Created vdus of vnfd:"+vnfr.getName());
                vnfrs.addVirtualDeploymentUnitShort(vdus);
            }
            nsrs.addVNFS(vnfrs);
        }
        this.networkServiceRecordShortList.add(nsrs);
        return nsrs;
    }

    @Override
    public void unManageNSR(NetworkServiceRecord networkServiceRecord) throws MonitoringException {

        monitoringManager.stopMonitorNS(networkServiceRecord);

        ScheduledFuture<?> future = futures.get(networkServiceRecord.getId());
        if(future!=null)
            future.cancel(true);
        futures.remove(networkServiceRecord.getId());
    }

    @Override
    public boolean isNSRManaged(String id) {
        for (NetworkServiceRecordShort nsrs : networkServiceRecordShortList){
            if(nsrs.getId().equals(id))
                return true;
        }
        return false;
    }

    @Override
    public boolean isAVNFAlarm(String id) {
        return monitoringManager.isVNFThreshold(id);
    }

    @Override
    public VRFaultManagementPolicy getVNFFaultManagementPolicy(String vnfFMPolicyId) {
                for(VRFaultManagementPolicy vnfFMPolicy : getVNFRShort(vnfFMPolicyId).getVnfFaultManagementPolicies()) {
                    if (vnfFMPolicy.getId().equals(vnfFMPolicyId)) {
                        return vnfFMPolicy;
                    }
                }
        return null;
    }
    @Override
    public VirtualNetworkFunctionRecordShort getVNFRShort(String vnfFMPolicyId) {
        for(NetworkServiceRecordShort nsrs : networkServiceRecordShortList){
            for(VirtualNetworkFunctionRecordShort vnfrs: nsrs.getVirtualNetworkFunctionRecordShorts()){
                for(VRFaultManagementPolicy vnfFMPolicy : vnfrs.getVnfFaultManagementPolicies()){
                    if(vnfFMPolicy.getId().equals(vnfFMPolicyId)){
                        return vnfrs;
                    }
                }
            }
        }
        return null;
    }

    public String getVnfrIdByPolicyId(String policyId){
        return getVNFRShort(policyId).getId();
    }

    @Override
    public String getPolicyIdByThresholdId(String triggerId) {

        return monitoringManager.getPolicyIdFromTrhresholdId(triggerId);
    }

    private class HighConfigurator implements Runnable {
        private NetworkServiceRecord nsr;

        public HighConfigurator(NetworkServiceRecord nsr) {
            this.nsr = nsr;
        }

        @Override
        public void run() {
            NetworkServiceRecord nsr = null;
            try {
                nsr = NFVORequestorWrapper.getNetworkServiceRecord(this.nsr.getId());
            } catch (NotFoundException e) {
                log.error(e.getMessage(),e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SDKException e) {
                e.printStackTrace();
            }
            if(nsr.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
                log.debug("Redundancy thread: the nsr to check redundancy is not in ACTIVE state");
                return;
            }
            for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
                try {
                    // If there are instances to be removed
                    String failedVNFCInstance = highAvailabilityManager.cleanFailedInstances(vnfr);
                    if (failedVNFCInstance!=null){
                        log.info("Cleaning procedure start for: "+failedVNFCInstance);
                        monitoringManager.removeMonitoredVnfcInstance(failedVNFCInstance);
                    }
                    else{
                        highAvailabilityManager.configureRedundancy(vnfr);
                    }
                } catch (HighAvailabilityException e) {
                    log.error("Configuration of the redundancy for the vnfr: "+vnfr.getName()+" "+e.getMessage(),e);
                }catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
