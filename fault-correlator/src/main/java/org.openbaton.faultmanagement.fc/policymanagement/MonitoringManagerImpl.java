package org.openbaton.faultmanagement.fc.policymanagement;

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
import org.openbaton.faultmanagement.fc.interfaces.NFVORequestor;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.MonitoringManager;
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
    private static final String monitorApiUrl="localhost:8090";
    private Map<String,ScheduledFuture<?>> futures;
    private Map<String,List<String> > vduIdPmJobIdMap;
    private Set<String> vnfTriggerId;
    private Map<String,List<String> > thresholdIdListHostname;
    private Map<String, String> thresholdIdFMPolicyId;
    private MonitoringPluginCaller monitoringPluginCaller;
    @Autowired private NFVORequestor NFVORequestor;
    @Value("${fms.monitoringcheck:60}")
    private String monitoringCheck;

    @PostConstruct
    public void init() throws NotFoundException {
        futures=new HashMap<>();
        vduIdPmJobIdMap=new HashMap<>();
        thresholdIdListHostname= new HashMap<>();
        thresholdIdFMPolicyId= new HashMap<>();
        vnfTriggerId= new HashSet<>();
        try {
            monitoringPluginCaller = new MonitoringPluginCaller("zabbix","zabbix-plugin");
        } catch (TimeoutException e) {
            log.error(e.getMessage(),e);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.debug("monitoringplugincaller obtained");
    }

    @Override
    public void startMonitorNS(NetworkServiceRecord nsr){
        MonitoringThreadCreator mpc = new MonitoringThreadCreator(nsr.getId());
        int interval = Integer.parseInt(monitoringCheck);
        // Wait 10 seconds for the host registration in zabbix server. And then schedule the monitor creator at fixed rate
        futures.put(nsr.getId(), nsScheduler.scheduleAtFixedRate(mpc, 10,interval, TimeUnit.SECONDS));
    }
    public void removeMonitoredVnfcInstance(String vnfcInstanceHostname){
        if(vnfcInstanceHostname==null)
            throw new NullPointerException("The vnfcInstanceHostname is null");
        if(!isVNFCMonitored(vnfcInstanceHostname)) {
            log.warn("The vnfc of name: "+vnfcInstanceHostname+" is not monitored");
            return;
        }
        log.debug("Cleaning for: "+vnfcInstanceHostname);
        Iterator<Map.Entry<String,List<String>>> it = thresholdIdListHostname.entrySet().iterator();
        List<String> triggerIdsToRemove=new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry<String,List<String>> pair = it.next();
            //We are assuming that there is a threshold for each vnfcinstance
            if(pair.getValue().contains(vnfcInstanceHostname)) {
                String triggerIdToRemove = pair.getKey();
                triggerIdsToRemove.add(triggerIdToRemove);
                log.debug("Removing entry : "+pair);
                it.remove();
            }
        }

        for(String tid: triggerIdsToRemove){
            log.debug("Removing trigger id : "+tid);
            thresholdIdFMPolicyId.remove(tid);
            vnfTriggerId.remove(tid);
        }
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

    public List<String> getHostnamesFromThresholdId(String thresholdId){
        return thresholdIdListHostname.get(thresholdId);
    }
    public String getPolicyIdFromTrhresholdId(String thresholdId){
        return thresholdIdFMPolicyId.get(thresholdId);
    }

    private boolean isVNFCMonitored(String hostname){
        boolean found=false;
        for(Map.Entry<String,List<String>> entry : thresholdIdListHostname.entrySet()){
            if(entry.getValue().contains(hostname))
                found = true;
        }
        return found;
    }

    @Override
    public boolean isVNFThreshold(String thresholdId) {
        return vnfTriggerId.contains(thresholdId);
    }

    private class MonitoringThreadCreator implements Runnable{
        private String nsrId;
        public MonitoringThreadCreator(String nsrId) {
            this.nsrId=nsrId;
        }

        @Override
        public void run() {
            try {
                NetworkServiceRecord nsr = NFVORequestor.getNetworkServiceRecord(this.nsrId);
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
                        String pmJobId = monitoringPluginCaller.createPMJob(objectSelection, monitoringParamentersLIst, new ArrayList<String>(), 30, 0);
                        savePmJobId(vdu.getId(), pmJobId);

                        //create all pm job with a custom period in the criteria
                        Set<String> monitoringParameterWithPeriod = vdu.getMonitoring_parameter();
                        monitoringParameterWithPeriod.removeAll(monitoringParamentersWithoutPeriod);

                        for (String mpwp : monitoringParameterWithPeriod) {
                            int period = getPeriodFromThreshold(mpwp, vdu.getFault_management_policy());
                            monitoringParamentersLIst.clear();
                            monitoringParamentersLIst.add(mpwp);
                            //log.debug("This monitoringParameter: " + mpwp + " has custom period of: " + period + " seconds");
                            pmJobId = monitoringPluginCaller.createPMJob(objectSelection, monitoringParamentersLIst, new ArrayList<String>(), period, 0);
                            savePmJobId(vdu.getId(), pmJobId);
                        }
                        if(vdu.getFault_management_policy()!=null)
                            for (VRFaultManagementPolicy vnffmp : vdu.getFault_management_policy()) {
                                String thresholdId="";
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
                                            thresholdIdListHostname.put(thresholdId, objs.getObjectInstanceIds());
                                            thresholdIdFMPolicyId.put(thresholdId, vnffmp.getId());
                                            if(vnffmp.getName().startsWith("VNF")){
                                                log.debug("VNF threshold id: "+thresholdId);
                                                vnfTriggerId.add(thresholdId);
                                            }
                                        }
                                    else {
                                        thresholdId = monitoringPluginCaller.createThreshold(objectSelection, performanceMetric, ThresholdType.SINGLE_VALUE, thresholdDetails);
                                        thresholdIdListHostname.put(thresholdId, objectSelection.getObjectInstanceIds());
                                        thresholdIdFMPolicyId.put(thresholdId, vnffmp.getId());
                                        if(vnffmp.getName().startsWith("VNF")){
                                            log.debug("VNF threshold id: "+thresholdId);
                                            vnfTriggerId.add(thresholdId);
                                        }
                                    }

                                }
                            }
                    }
                }
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            } catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }


        private void savePmJobId(String vduId,String pmJobId){
            if(vduIdPmJobIdMap.get(vduId)==null){
                List<String> pmjobIds = new ArrayList<>();
                pmjobIds.add(pmJobId);
                vduIdPmJobIdMap.put(vduId,pmjobIds);
            }
            else {
                List<String> pmjobIds = vduIdPmJobIdMap.get(vduId);
                pmjobIds.add(pmJobId);
                vduIdPmJobIdMap.put(vduId,pmjobIds);
            }
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
            List<String> thresholdIdsToRemove= new ArrayList<>();
            List<String> pmJobIdsToRemove= new ArrayList<>();
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu())
                    for(VRFaultManagementPolicy fmp : vdu.getFault_management_policy()){
                        for(Map.Entry<String,String> entry : thresholdIdFMPolicyId.entrySet()){
                            if(entry.getValue().equalsIgnoreCase(fmp.getId())){
                                thresholdIdsToRemove.add(entry.getKey());
                            }
                        }
                    }
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                    if(vduIdPmJobIdMap.get(vdu.getId())!=null){
                        pmJobIdsToRemove.addAll(vduIdPmJobIdMap.get(vdu.getId()));
                    }
                }
            }
            // removing thresholds

            List<String> idsRemoved= new ArrayList<>();
            /*try {
                idsRemoved = monitoringPluginCaller.deleteThreshold(thresholdIdsToRemove);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }
            if(idsRemoved.size()!=thresholdIdsToRemove.size()){
                thresholdIdsToRemove.removeAll(idsRemoved);
                log.warn("Removed less thresholds.. These thresholds have not been deleted: "+thresholdIdsToRemove);
            }else log.debug("Removed all the thresholds: "+ idsRemoved);*/
            // clean local state
            for (String thresholdIdRemoved : thresholdIdsToRemove){
                thresholdIdFMPolicyId.remove(thresholdIdRemoved);
                thresholdIdListHostname.remove(thresholdIdRemoved);
            }
            // removing pmJobs
            idsRemoved.clear();
            /*try {
                idsRemoved=monitoringPluginCaller.deletePMJob(pmJobIdsToRemove);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }*/
            if(idsRemoved.size()!=pmJobIdsToRemove.size()){
                pmJobIdsToRemove.removeAll(idsRemoved);
                //log.warn("Removed less pmJobs.. These pmjobs have not been deleted: "+pmJobIdsToRemove);
            }else log.debug("Removed all the pmjobs: "+ pmJobIdsToRemove);
            // clean local state
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VirtualDeploymentUnit vdu : vnfr.getVdu()) {
                    vduIdPmJobIdMap.remove(vdu.getId());
                }
            }

        }

    }



}
