package org.openbaton.faultmanagement.fc.policymanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.*;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.MonitoringManager;
import org.openbaton.monitoring.interfaces.MonitoringPluginCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by mob on 04.11.15.
 */
@Service
public class MonitoringManagerImpl implements MonitoringManager {
    private static final Logger log = LoggerFactory.getLogger(MonitoringManagerImpl.class);
    private final ScheduledExecutorService nsScheduler = Executors.newScheduledThreadPool(1);
    private static final String monitorApiUrl="localhost:8090";
    private Map<String,ScheduledFuture<?>> futures;
    private Map<String,List<String> > vduIdPmJobIdMap;
    private Map<String,List<String> > thresholdIdListHostname;
    private Map<String, String> thresholdIdFMPolicyId;
    private MonitoringPluginCaller monitoringPluginCaller;

    @PostConstruct
    public void init() throws NotFoundException {
        futures=new HashMap<>();
        vduIdPmJobIdMap=new HashMap<>();
        thresholdIdListHostname= new HashMap<>();
        thresholdIdFMPolicyId= new HashMap<>();
        try {
            monitoringPluginCaller = new MonitoringPluginCaller("zabbix","15672");
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
        MonitoringThreadCreator mpc = new MonitoringThreadCreator(nsr);
        // Wait 5 seconds for the host registration in zabbix server.
        futures.put(nsr.getId(), nsScheduler.schedule(mpc, 5, TimeUnit.SECONDS));
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

    private class MonitoringThreadCreator implements Runnable{
        private NetworkServiceRecord nsr;
        public MonitoringThreadCreator(NetworkServiceRecord nsr) {
            this.nsr=nsr;
        }

        @Override
        public void run() {
            try {
                for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){

                    //Note: We consider there will be only one vdu per vnf !
                    VirtualDeploymentUnit vdu = vnfr.getVdu().iterator().next();

                    ObjectSelection objectSelection = new ObjectSelection();
                    for (VNFCInstance vnfcInstance: vdu.getVnfc_instance() ){
                        log.debug("vnfcinstance name: "+vnfcInstance.getHostname());
                        objectSelection.addObjectInstanceId(vnfcInstance.getHostname());
                    }

                    List<String> monitoringParamentersLIst=new ArrayList<>();
                    Set<String> monitoringParamentersWithoutPeriod = getMonitoringParamentersWithoutPeriod(vdu.getMonitoring_parameter(),vnfr);
                    monitoringParamentersLIst.addAll(monitoringParamentersWithoutPeriod);
                    //One pmJob per vdu (Actually)
                    //create all pm job without a custom period in the criteria
                    //default period is 30 seconds
                    String pmJobId = monitoringPluginCaller.createPMJob(objectSelection,monitoringParamentersLIst,new ArrayList<String>(),30,0);
                    savePmJobId(vdu.getId(), pmJobId);

                    //create all pm job with a custom period in the criteria
                    Set<String> monitoringParameterWithPeriod = vdu.getMonitoring_parameter();
                    monitoringParameterWithPeriod.removeAll(monitoringParamentersWithoutPeriod);

                    for(String mpwp : monitoringParameterWithPeriod){
                        int period = getPeriodFromThreshold(mpwp,vnfr.getFault_management_policy());
                        monitoringParamentersLIst.clear();monitoringParamentersLIst.add(mpwp);
                        log.debug("This monitoringParameter: "+mpwp+" has custom period of: "+period+" seconds");
                        pmJobId = monitoringPluginCaller.createPMJob(objectSelection,monitoringParamentersLIst,new ArrayList<String>(),period,0);
                        savePmJobId(vdu.getId(),pmJobId);
                    }

                    for (VNFFaultManagementPolicy vnffmp : vnfr.getFault_management_policy()){
                        for(Criteria criteria: vnffmp.getCriteria()){
                            String performanceMetric = criteria.getParameter_ref();
                            String function = criteria.getFunction();
                            String hostOperator = criteria.getVnfc_selector() == VNFCSelector.all ? "&" : "|";
                            ThresholdDetails thresholdDetails= new ThresholdDetails(function,criteria.getComparison_operator(),vnffmp.getSeverity(),criteria.getThreshold(),hostOperator);
                            String thresholdId = monitoringPluginCaller.createThreshold(objectSelection, performanceMetric, ThresholdType.SINGLE_VALUE, thresholdDetails);
                            thresholdIdListHostname.put(thresholdId,objectSelection.getObjectInstanceIds());
                            thresholdIdFMPolicyId.put(thresholdId,vnffmp.getId());
                        }
                    }

                }
                log.debug("End MonitoringThreadCreator for the nsr with name: "+nsr.getName());
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
        private Set<String> getMonitoringParamentersWithoutPeriod(Set<String> monitoring_parameter, VirtualNetworkFunctionRecord vnfr) {
            Set<String> result = new HashSet<>(monitoring_parameter);
            Set<String> tmp = new HashSet<>();
            Iterator<String> iterator= result.iterator();
            while(iterator.hasNext()) {
                String currentMonitoringParameter = iterator.next();
                for (VNFFaultManagementPolicy vnffmp : vnfr.getFault_management_policy()) {
                    for (Criteria c : vnffmp.getCriteria()) {
                        if (c.getParameter_ref().equalsIgnoreCase(currentMonitoringParameter)){
                            tmp.add(c.getParameter_ref());
                        }
                    }
                }
            }
            result.removeAll(tmp);
            return result;
        }

        private int getPeriodFromThreshold(String mpwp,Set<VNFFaultManagementPolicy> fault_management_policy) throws MonitoringException {
            for (VNFFaultManagementPolicy vnffmp : fault_management_policy) {
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
                for(VNFFaultManagementPolicy vnffmp : vnfr.getFault_management_policy()){
                    for(Map.Entry<String,String> entry : thresholdIdFMPolicyId.entrySet()){
                        if(entry.getValue().equalsIgnoreCase(vnffmp.getId())){
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
            try {
                idsRemoved = monitoringPluginCaller.deleteThreshold(thresholdIdsToRemove);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }
            if(idsRemoved.size()!=thresholdIdsToRemove.size()){
                thresholdIdsToRemove.removeAll(idsRemoved);
                log.warn("Removed less thresholds.. These thresholds have not been deleted: "+thresholdIdsToRemove);
            }else log.debug("Removed all the thresholds: "+ idsRemoved);
            // clean local state
            for (String thresholdIdRemoved : thresholdIdsToRemove){
                thresholdIdFMPolicyId.remove(thresholdIdRemoved);
                thresholdIdListHostname.remove(thresholdIdRemoved);
            }
            // removing pmJobs
            idsRemoved.clear();
            try {
                idsRemoved=monitoringPluginCaller.deletePMJob(pmJobIdsToRemove);
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            }
            if(idsRemoved.size()!=pmJobIdsToRemove.size()){
                pmJobIdsToRemove.removeAll(idsRemoved);
                log.warn("Removed less pmJobs.. These pmjobs have not been deleted: "+pmJobIdsToRemove);
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
