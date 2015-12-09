package org.openbaton.faultmanagement.fc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementVNFCAction;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.VirtualizedResourceAlarmStateChangedNotification;
import org.openbaton.catalogue.mano.common.monitoring.Alarm;
import org.openbaton.catalogue.mano.common.monitoring.AlarmState;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.messages.OrVnfmHealVNFRequestMessage;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualDeploymentUnitShort;
import org.openbaton.faultmanagement.fc.policymanagement.catalogue.VirtualNetworkFunctionRecordShort;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.MonitoringManager;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.fc.repositories.AlarmRepository;
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
    private AlarmRepository alarmRepository;

    @Autowired
    private MonitoringManager monitoringManager;

    @Autowired
    private PolicyManager policyManager;

    @PostConstruct
    public void init(){
        mapper= new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void newVnfAlarm(Alarm vnfAlarm) {
        log.debug("New VNF alarm: "+vnfAlarm);

        if(vnfAlarm.getPerceivedSeverity().ordinal()== PerceivedSeverity.CRITICAL.ordinal()){
            //check if there are alarms in the same vm
            // get vnfr
            //List<Alarm> activeAlarms = alarmRepository.findByResourceIdAndAlarmStateNot("hostname",AlarmState.CLEARED);
            /*if(activeAlarms.isEmpty()){
                //get VNFFaultManagementPolicy
                //executeActionInVNFPolicy(VNFFaultManagementPolicy);
            }*/
        }
    }

    @Override
    public void newVRAlarm(Alarm vrAlarm) {
        log.debug("New VR alarm: "+vrAlarm);
        executeVNFPolicy(vrAlarm.getTriggerId());
    }
    @Override
    public void updateStatusVRAlarm(VirtualizedResourceAlarmStateChangedNotification vrascn) {
        log.debug("VR state changed notification: "+vrascn);
        if(vrascn.getAlarmState().ordinal() == AlarmState.UPDATED.ordinal())
            executeVNFPolicy(vrascn.getTriggerId());
        else if (vrascn.getAlarmState().ordinal() == AlarmState.CLEARED.ordinal()) {
            log.debug("The alarm has been cleared");
            Alarm clearedAlarm = alarmRepository.findFirstByTriggerId(vrascn.getTriggerId());
            log.debug("Details of the alarm:\n"+clearedAlarm);
        }
    }
    private void executeVNFPolicy(String triggerId) {
        List<String> hostnames;
        if(triggerId!=null) {
            hostnames = monitoringManager.getHostnamesFromThresholdId(triggerId);
            if(hostnames!=null) {
                log.debug("This is a VNF alarm coming from the hostnames: " + hostnames);
                log.debug("Getting the VNF faultManagementPolicy");
                String policyId = monitoringManager.getPolicyIdFromTrhresholdId(triggerId);
                VNFFaultManagementPolicy vnfFaultManagementPolicy = policyManager.getVNFFaultManagementPolicy(policyId);
                FaultManagementVNFCAction action = vnfFaultManagementPolicy.getAction();
                log.debug("this action need to be executed: " + action);

                OrVnfmHealVNFRequestMessage healMessage = getHealMessage(vnfFaultManagementPolicy.getName());
                VirtualNetworkFunctionRecordShort vnfrs = policyManager.getVNFRShort(policyId);
                VirtualDeploymentUnitShort vdus=vnfrs.getVirtualDeploymentUnitShorts().iterator().next();
                for(String vnfcInstanceId : vdus.getVNFCInstanceIdFromHostname(hostnames))
                    sendHealMessage(healMessage,vnfrs.getNsrFatherId(),vnfrs.getId(),vdus.getId(),vnfcInstanceId);
            }
        }
    }

    private void sendHealMessage(OrVnfmHealVNFRequestMessage healMessage,String ... ids) {
        HttpResponse<String> jsonResponse=null;
        String finalUrl=nfvoUrl;
        finalUrl += "/"+ids[0];
        finalUrl += ids[1]==null ? "" : "/vnfrecords/"+ids[1];
        finalUrl += ids[2]==null ? "" : "/vdunits/"+ids[2];
        finalUrl += ids[3]==null ? "" : "/vnfcinstances/"+ids[3];
        finalUrl+="/actions";
        log.debug("Posting action heal at url: "+finalUrl);
        String jsonMessage= mapper.toJson(healMessage,OrVnfmHealVNFRequestMessage.class);
        try {
            jsonResponse = Unirest.post(finalUrl).header("Content-type","application/json").header("KeepAliveTimeout","5000").body(jsonMessage).asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        log.debug("Response from heal function: "+jsonResponse.getBody());
    }

    private OrVnfmHealVNFRequestMessage getHealMessage(String cause) {
        OrVnfmHealVNFRequestMessage orVnfmHealVNFRequestMessage = new OrVnfmHealVNFRequestMessage();
        orVnfmHealVNFRequestMessage.setAction(Action.HEAL);
        orVnfmHealVNFRequestMessage.setCause(cause);
        return orVnfmHealVNFRequestMessage;
    }

    @Override
    public void updateStatusVnfAlarm(VNFAlarmStateChangedNotification vnfascn) {

    }


}
