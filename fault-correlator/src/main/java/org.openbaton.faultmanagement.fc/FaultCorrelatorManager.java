package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementAction;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VRFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.faultmanagement.fc.exceptions.FaultCorrelatorException;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualDeploymentUnitShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.MonitoringManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.ha.HighAvailabilityManager;
import org.openbaton.faultmanagement.ha.exceptions.HighAvailabilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by mob on 09.11.15.
 */
@Service
public class FaultCorrelatorManager implements org.openbaton.faultmanagement.fc.interfaces.FaultCorrelatorManager{

    private static final Logger log = LoggerFactory.getLogger(FaultCorrelatorManager.class);
    private static final String nfvoUrl = "http://localhost:8080/api/v1/ns-records";
    private Gson mapper;


    @Autowired
    private MonitoringManager monitoringManager;

    @Autowired
    private NSRManager nsrManager;

    @Autowired
    private HighAvailabilityManager highAvailabilityManager;

    @Autowired
    private PolicyManager policyManager;

    @PostConstruct
    public void init(){
        mapper= new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void newVnfAlarm(Alarm vnfAlarm) {
        log.debug("New VNF alarm: "+vnfAlarm);

        // FAULT CORRELATION
        //check if there are alarms in the same vm

        //List<Alarm> activeAlarms = alarmRepository.findByResourceIdAndAlarmStateNotAndAlarmType(vnfAlarm.getThresholdId(),AlarmState.CLEARED, AlarmType.VIRTUALIZED_RESOURCE);




        /*for(Alarm activeVRAlarm : activeAlarms){
            if(activeVRAlarm.getPerceivedSeverity().ordinal()>=vnfAlarm.getPerceivedSeverity().ordinal()){
                log.info("There is an active VR alarm with "+ activeVRAlarm.getPerceivedSeverity()+" severity on the same vm of the VNFC "+vnfAlarm);
                if(activeVRAlarm.getFaultType().ordinal()==vnfAlarm.getFaultType().ordinal()){
                    log.info("The VR fault is of type "+ activeVRAlarm.getFaultType()+" the same that the VNFC, can be a correlation");
                }
                log.info("No actions are executed");
                return;
            }
        }

        if(activeAlarms.isEmpty()){
            try {
                executeVNFPolicy(vnfAlarm.getThresholdId());
            } catch (FaultCorrelatorException e) {
                log.error(e.getMessage(),e);
            } catch (HighAvailabilityException e) {
                log.error(e.getMessage(),e);
            }
        }*/
        // END FAULT CORRELATION

    }

    @Override
    public void newVRAlarm(Alarm vrAlarm) {
        log.debug("New VR alarm: \n"+vrAlarm);

    }

    @Override
    public void updateStatusVRAlarm(VirtualizedResourceAlarmStateChangedNotification vrascn) {
        //log.debug("VR state changed notification: "+vrascn);
        /*try {
            if(vrascn.getAlarmState().ordinal() == AlarmState.UPDATED.ordinal())

                executeVNFPolicy(vrascn.getThresholdId());

            else if (vrascn.getAlarmState().ordinal() == AlarmState.CLEARED.ordinal()) {
                log.debug("The alarm has been cleared");
                Alarm clearedAlarm = alarmRepository.findFirstByTriggerId(vrascn.getThresholdId());
                log.debug("Details of the alarm:\n"+clearedAlarm);
            }
        } catch (FaultCorrelatorException e) {
            log.error(e.getMessage(),e);
        } catch (HighAvailabilityException e) {
            log.error(e.getMessage(),e);
        }*/
    }
    private void executeVNFPolicy(String triggerId) throws FaultCorrelatorException, HighAvailabilityException {
        List<String> hostnames;
        if(triggerId!=null) {
            hostnames = monitoringManager.getHostnamesFromThresholdId(triggerId);
            if(hostnames!=null) {
                String policyId = monitoringManager.getPolicyIdFromTrhresholdId(triggerId);
                VRFaultManagementPolicy VRFaultManagementPolicy = policyManager.getVNFFaultManagementPolicy(policyId);
                log.debug("This is a VNF alarm coming from the hostnames: " + hostnames);
                FaultManagementAction action = VRFaultManagementPolicy.getAction();
                log.debug("this action need to be executed: " + action);

                if (action.ordinal()== FaultManagementAction.SWITCH_TO_STANDBY.ordinal()){
                    VirtualNetworkFunctionRecordShort vnfrs=policyManager.getVNFRShort(policyId);
                    VirtualNetworkFunctionRecord vnfr = nsrManager.getVirtualNetworkFunctionRecord(vnfrs.getNsrFatherId(),vnfrs.getId());
                    //find the standby VNFC instance
                    if(vnfr.getId()==null)
                        throw new FaultCorrelatorException("The vnfr retrieved from the nfvo has no id");
                    for(VirtualDeploymentUnit vdu: vnfr.getVdu()){
                        for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                            if(hostnames.contains(vnfcInstance.getHostname())){
                                VNFCInstance vnfcInstanceStandby = getStandbyVNFCInstance(vdu);
                                //highAvailabilityManager.switchToRedundantVNFC(vnfr,vdu,vnfcInstanceStandby);
                            }
                        }
                    }
                }
            }
        }
    }

    private VNFCInstance getStandbyVNFCInstance(VirtualDeploymentUnit vdu) throws FaultCorrelatorException {
        for(VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
            if(vnfcInstance.getState()!=null && vnfcInstance.getState().equals("standby"))
                return vnfcInstance;
        }
        throw new FaultCorrelatorException("No VNFCInstance found in standby mode");
    }





    @Override
    public void updateStatusVnfAlarm(VNFAlarmStateChangedNotification vnfascn) {
        /*log.debug("VR state changed notification: "+vnfascn);
        try {
            if(vnfascn.getAlarmState().ordinal() == AlarmState.UPDATED.ordinal())

                executeVNFPolicy(vnfascn.getFmPolicyId());

            else if (vnfascn.getAlarmState().ordinal() == AlarmState.CLEARED.ordinal()) {
                log.debug("The VNF alarm has been cleared");
                Alarm clearedAlarm = alarmRepository.findFirstByTriggerId(vnfascn.getFmPolicyId());
                log.debug("Details of the alarm:\n"+clearedAlarm);
            }
        } catch (FaultCorrelatorException e) {
            log.error(e.getMessage(),e);
        } catch (HighAvailabilityException e) {
            log.error(e.getMessage(),e);
        }*/
    }


}
