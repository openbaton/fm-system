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

package org.openbaton.faultmanagement.core.mm;

import org.openbaton.catalogue.mano.common.faultmanagement.Criteria;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFCSelector;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.monitoring.ObjectSelection;
import org.openbaton.catalogue.mano.common.monitoring.ThresholdDetails;
import org.openbaton.catalogue.mano.common.monitoring.ThresholdType;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.catalogue.ManagedNetworkServiceRecord;
import org.openbaton.faultmanagement.catalogue.ThresholdHostnames;
import org.openbaton.faultmanagement.repo.ManagedNetworkServiceRecordRepository;
import org.openbaton.faultmanagement.requestor.interfaces.NFVORequestorWrapper;
import org.openbaton.faultmanagement.core.mm.interfaces.MonitoringManager;
import org.openbaton.monitoring.interfaces.MonitoringPluginCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by mob on 04.11.15.
 */
@Service
@ConfigurationProperties
public class MonitoringManagerImpl implements MonitoringManager {
    private static final Logger log = LoggerFactory.getLogger(MonitoringManagerImpl.class);
    private final ScheduledExecutorService nsScheduler = Executors.newScheduledThreadPool(1);
    private Map<String,ScheduledFuture<?>> futures;
    private MonitoringPluginCaller monitoringPluginCaller;
    private static int defaultPeriod=30;
    @Autowired private ManagedNetworkServiceRecordRepository mnsrRepo;
    @Autowired private NFVORequestorWrapper NFVORequestorWrapper;
    @Value("${fms.monitoringcheck:60}")
    private String monitoringCheck;

    @PostConstruct
    public void init() throws NotFoundException {
        futures=new HashMap<>();
        try {
            monitoringPluginCaller = new MonitoringPluginCaller("zabbix","zabbix-plugin");
        } catch (TimeoutException|IOException e) {
            log.error(e.getMessage(),e);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        log.debug("MonitoringPluginCaller obtained");
    }

    @Override
    public void startMonitorNS(NetworkServiceRecord nsr){
        MonitoringThreadCreator mpc = new MonitoringThreadCreator(nsr.getId());
        int interval = Integer.parseInt(monitoringCheck);
        // Wait 10 seconds for the host registration in zabbix server. And then schedule the monitor creator at fixed rate
        futures.put(nsr.getId(), nsScheduler.scheduleAtFixedRate(mpc, 10,interval, TimeUnit.SECONDS));
    }

    private void removeMonitoredVnfcInstance(String nsrId, String vnfcInstanceHostname){
        if(vnfcInstanceHostname==null)
            throw new NullPointerException("The vnfcInstanceHostname is null");
        if(!isVNFCMonitored(vnfcInstanceHostname)) {
            log.warn("The vnfc of name: "+vnfcInstanceHostname+" is not monitored");
            return;
        }

        log.debug("Cleaning for: "+vnfcInstanceHostname);
        ManagedNetworkServiceRecord mnsr=mnsrRepo.findByNsrId(nsrId);
        Iterator<Map.Entry<String,ThresholdHostnames>> it = mnsr.getHostnames().entrySet().iterator();
        List<String> triggerIdsToRemove=new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry<String,ThresholdHostnames> pair = it.next();
            //We are assuming that there is a threshold for each vnfcinstance
            if(pair.getValue().getHostnames().contains(vnfcInstanceHostname)) {
                String triggerIdToRemove = pair.getKey();
                triggerIdsToRemove.add(triggerIdToRemove);
                log.debug("Removing entry : "+pair);
                it.remove();
            }
        }

        for(String tid: triggerIdsToRemove){
            log.debug("Removing trigger id : "+tid);
            mnsr.getThresholdIdFmPolicyMap().remove(tid);
            mnsr.getVnfTriggerId().remove(tid);
        }
        mnsrRepo.save(mnsr);
    }
    @Override
    public void stopMonitorNS(NetworkServiceRecord nsr)  {
        ScheduledFuture<?> future = futures.get(nsr.getId());
        if(future!=null)
            future.cancel(true);
        futures.remove(nsr.getId());

        MonitoringThreadDeletor mpd = new MonitoringThreadDeletor(nsr);
        nsScheduler.schedule(mpd, 1, TimeUnit.SECONDS);
    }

    public String getPolicyIdFromTrhresholdId(String thresholdId){
        for( ManagedNetworkServiceRecord mnsr : mnsrRepo.findAll()){
            if(mnsr.getThresholdIdFmPolicyMap().get(thresholdId)!=null)
                return mnsr.getThresholdIdFmPolicyMap().get(thresholdId);
        }
        return null;
    }

    private boolean isVNFCMonitored(String hostname){
        for( ManagedNetworkServiceRecord mnsr : mnsrRepo.findAll()){
            //log.debug("found :" +mnsr);
            for(Map.Entry<String,ThresholdHostnames> entry : mnsr.getHostnames().entrySet()){
                if(entry.getValue().getHostnames().contains(hostname))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isVNFThreshold(String thresholdId) {
        for( ManagedNetworkServiceRecord mnsr : mnsrRepo.findAll()){
            if(mnsr.getVnfTriggerId().contains(thresholdId))
                return true;
        }
        return false;
    }

    private class MonitoringThreadCreator implements Runnable{
        private String nsrId;
        public MonitoringThreadCreator(String nsrId) {
            this.nsrId=nsrId;
        }

        @Override
        public void run() {
            try {
                NetworkServiceRecord nsr = NFVORequestorWrapper.getNsr(this.nsrId);
                if(nsr.getStatus().ordinal() != Status.ACTIVE.ordinal()) {
                    log.debug("the nsr to be monitored is not in ACTIVE state");
                    return;
                }
                for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){

                    for(VirtualDeploymentUnit vdu : vnfr.getVdu()) {

                        //Check if the vdu needs to be monitored
                        if(vdu.getMonitoring_parameter()==null)
                            continue;
                        if(vdu.getMonitoring_parameter().isEmpty())
                            continue;

                        ObjectSelection objectSelection = new ObjectSelection();
                        for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                            //Check if the vnfcInstance is not in standby
                            if(vnfcInstance.getState() != null && vnfcInstance.getState().equals("standby"))
                            {
                                continue;
                            }
                            else if (vnfcInstance.getState() != null && vnfcInstance.getState().equals("failed")){
                                removeMonitoredVnfcInstance(nsrId,vnfcInstance.getHostname());
                                continue;
                            }
                            //Check if the vnfcInstance is already monitored
                            if(isVNFCMonitored(vnfcInstance.getHostname())){
                                continue;
                            }
                            objectSelection.addObjectInstanceId(vnfcInstance.getHostname());
                        }


                        if(objectSelection.getObjectInstanceIds().isEmpty())
                            continue;
                        log.debug("the vnfc instances to be monitored are: " + objectSelection.getObjectInstanceIds());

                        Set<String> monitoringParamentersWithoutPeriod = getMonitoringParamentersWithoutPeriod(vdu.getMonitoring_parameter(), vdu);
                        //log.debug("monitoring Paramenters Without period: " + monitoringParamentersWithoutPeriod);
                        List<String> monitoringParamentersLIst = new ArrayList<>();
                        monitoringParamentersLIst.addAll(monitoringParamentersWithoutPeriod);
                        //One pmJob per vdu (Actually)
                        //create a pm job with all the items without a custom period in the criteria
                        //default period is 30 seconds
                        String pmJobId;
                        if(!monitoringParamentersLIst.isEmpty()) {
                            pmJobId = monitoringPluginCaller.createPMJob(objectSelection, monitoringParamentersLIst, new ArrayList<>(), defaultPeriod, 0);
                            savePmJobId(nsrId,vdu.getId(), pmJobId);
                        }

                        //create all pm job with a custom period in the criteria
                        Set<String> monitoringParameterWithPeriod = vdu.getMonitoring_parameter();
                        monitoringParameterWithPeriod.removeAll(monitoringParamentersWithoutPeriod);

                        for (String mpwp : monitoringParameterWithPeriod) {
                            int period = getPeriodFromThreshold(mpwp, vdu.getFault_management_policy());
                            monitoringParamentersLIst.clear();
                            monitoringParamentersLIst.add(mpwp);
                            //log.debug("This monitoringParameter: " + mpwp + " has custom period of: " + period + " seconds");
                            pmJobId = monitoringPluginCaller.createPMJob(objectSelection, monitoringParamentersLIst, new ArrayList<String>(), period, 0);
                            savePmJobId(nsrId,vdu.getId(), pmJobId);
                        }
                        if(vdu.getFault_management_policy()!=null)
                            for (VRFaultManagementPolicy vnffmp : vdu.getFault_management_policy()) {
                                String thresholdId;
                                for (Criteria criteria : vnffmp.getCriteria()) {
                                    String performanceMetric = criteria.getParameter_ref();
                                    String function = criteria.getFunction();
                                    String hostOperator = criteria.getVnfc_selector() == VNFCSelector.all ? "&" : "|";
                                    ThresholdDetails thresholdDetails = new ThresholdDetails(function, criteria.getComparison_operator(), vnffmp.getSeverity(), criteria.getThreshold(), hostOperator);

                                    if(criteria.getVnfc_selector() == VNFCSelector.at_least_one)
                                        for(String host: objectSelection.getObjectInstanceIds()){
                                            ObjectSelection objs = new ObjectSelection();
                                            objs.addObjectInstanceId(host);
                                            thresholdId = monitoringPluginCaller.createThreshold(objs, performanceMetric, ThresholdType.SINGLE_VALUE, thresholdDetails);
                                            mnsrRepo.addThresholdHostnames(nsrId,thresholdId, new ThresholdHostnames(objs.getObjectInstanceIds()));
                                            mnsrRepo.addFmPolicyId(nsrId,thresholdId, vnffmp.getId());

                                            if(vnffmp.isVNFAlarm()){
                                                log.debug("VNF threshold id: "+thresholdId);
                                               mnsrRepo.addVnfTriggerId(nsrId,thresholdId);
                                            }else log.debug("VR threshold id: "+thresholdId);

                                        }
                                    else {
                                        thresholdId = monitoringPluginCaller.createThreshold(objectSelection, performanceMetric, ThresholdType.SINGLE_VALUE, thresholdDetails);
                                        mnsrRepo.addThresholdHostnames(nsrId,thresholdId, new ThresholdHostnames(objectSelection.getObjectInstanceIds()));
                                        mnsrRepo.addFmPolicyId(nsrId,thresholdId, vnffmp.getId());
                                        if(vnffmp.isVNFAlarm()){
                                            log.debug("VNF threshold id: "+thresholdId);
                                            mnsrRepo.addVnfTriggerId(nsrId,thresholdId);
                                        }

                                    }

                                }
                            }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }


        private void savePmJobId(String nsrId, String vduId,String pmJobId){
            mnsrRepo.addPmJobId(nsrId,vduId,pmJobId);
        }
        private Set<String> getMonitoringParamentersWithoutPeriod(Set<String> monitoring_parameter, VirtualDeploymentUnit vdu) {
            Set<String> result = new HashSet<>(monitoring_parameter);
            Set<String> tmp = new HashSet<>();
            Iterator<String> iterator= result.iterator();
            while(iterator.hasNext()) {
                String currentMonitoringParameter = iterator.next();
                if(vdu.getFault_management_policy() == null )
                    break;
                for (VRFaultManagementPolicy vnffmp : vdu.getFault_management_policy()) {
                    for (Criteria c : vnffmp.getCriteria()) {
                        if (c.getParameter_ref().equalsIgnoreCase(currentMonitoringParameter) && vnffmp.getPeriod()!=0){
                            tmp.add(c.getParameter_ref());
                        }
                    }
                }
            }
            result.removeAll(tmp);
            return result;
        }

        private int getPeriodFromThreshold(String mpwp,Set<VRFaultManagementPolicy> fault_management_policy) throws MonitoringException {
            for (VRFaultManagementPolicy vnffmp : fault_management_policy) {
                for (Criteria c : vnffmp.getCriteria()) {
                    if (c.getParameter_ref().equalsIgnoreCase(mpwp)){
                        return vnffmp.getPeriod();
                    }
                }
            }
            throw new MonitoringException("no period found for the parameter"+ mpwp);
        }
    }

    private class MonitoringThreadDeletor implements Runnable{
        private NetworkServiceRecord nsr;
        private Logger log = LoggerFactory.getLogger(MonitoringThreadCreator.class);

        public MonitoringThreadDeletor(NetworkServiceRecord nsr) {
            this.nsr=nsr;
        }

        @Override
        public void run() {
            List<String> thresholdIdsToRemove = new ArrayList<>();
            List<String> pmJobIdsToRemove = new ArrayList<>();
            ManagedNetworkServiceRecord mnsr=mnsrRepo.findByNsrId(nsr.getId());
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu())
                    for(VRFaultManagementPolicy fmp : vdu.getFault_management_policy()){
                        for(Map.Entry<String,String> entry : mnsr.getThresholdIdFmPolicyMap().entrySet()){
                            if(entry.getValue().equalsIgnoreCase(fmp.getId())){
                                thresholdIdsToRemove.add(entry.getKey());
                            }
                        }
                    }
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    if(mnsr.getVduIdPmJobIdMap().get(vdu.getId())!=null){
                        pmJobIdsToRemove.addAll(mnsr.getVduIdPmJobIdMap().get(vdu.getId()).getPmJobsIds());
                    }
                }
            }

            // removing thresholds

            List<String> idsRemoved= new ArrayList<>();
            try {
                idsRemoved = monitoringPluginCaller.deleteThreshold(thresholdIdsToRemove);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }
            if(idsRemoved.size()!=thresholdIdsToRemove.size()){
                log.warn("Removed less thresholds..");
            }else log.info("Removed all the thresholds: "+ idsRemoved);

            // removing pmJobs
            idsRemoved.clear();
            try {
                idsRemoved=monitoringPluginCaller.deletePMJob(pmJobIdsToRemove);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }
            if(idsRemoved.size()!=pmJobIdsToRemove.size()){
                //pmJobIdsToRemove.removeAll(idsRemoved);
                log.warn("Removed less pmJobs..");
            }else log.debug("Removed all the pmjobs: "+ pmJobIdsToRemove);
            // clean local state
            mnsrRepo.delete(mnsr.getId());
        }

    }



}
