package org.openbaton.faultmanagement.fc.policymanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.*;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.exceptions.MonitoringException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.VnfFaultMonitor;
import org.openbaton.monitoring.interfaces.MonitoringPluginCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by mob on 04.11.15.
 */
@Service
public class VnfFaultMonitorImpl implements VnfFaultMonitor{
    protected static final Logger log = LoggerFactory.getLogger(VnfFaultMonitorImpl.class);
    private final ScheduledExecutorService vnfScheduler = Executors.newScheduledThreadPool(1);
    private static final String monitorApiUrl="localhost:8090";
    private Map<String,ScheduledFuture<?>> futures;
    private Map<String,String > vduIdPmJobIdMap;
    private MonitoringPluginCaller monitoringPluginCaller;
    protected ApplicationEventPublisher publisher;

    @PostConstruct
    public void init(){
        futures=new HashMap<>();
        vduIdPmJobIdMap=new HashMap<>();
        try {
            monitoringPluginCaller = new MonitoringPluginCaller("zabbix");
        } catch (TimeoutException e) {
            log.error(e.getMessage(),e);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.debug("monitoringplugincaller obtained");
    }

    public void startMonitorVNF(VirtualNetworkFunctionRecord vnfr){
        if(vnfr.getFault_management_policy()!=null){
            VNFPolicyCreator mpc = new VNFPolicyCreator(vnfr);
            futures.put(vnfr.getId(), vnfScheduler.schedule(mpc, 1, TimeUnit.SECONDS));
        }
    }

    @Override
    public void stopMonitorVNF(VirtualNetworkFunctionRecord vnfr) {

    }

    private class VNFPolicyCreator implements Runnable{
        private Set<VNFCInstance> vnfcInstances;
        private VirtualNetworkFunctionRecord vnfr;
        private Logger log = LoggerFactory.getLogger(VNFPolicyCreator.class);

        public VNFPolicyCreator(VirtualNetworkFunctionRecord vnfr) {
            this.vnfr=vnfr;
        }

        @Override
        public void run() {
            //Note: We consider there will be only one vdu per vnf !
            VirtualDeploymentUnit vdu = vnfr.getVdu().iterator().next();
            try {
                ObjectSelection objectSelection = new ObjectSelection();
                for (VNFCInstance vnfcInstance: vdu.getVnfc_instance() ){
                    log.debug("vnfcinstance name: "+vnfcInstance.getHostname());
                    objectSelection.addObjectInstanceId(vnfcInstance.getHostname());
                }
                List<String> monitoringParamenters=new ArrayList<>();
                monitoringParamenters.addAll(vdu.getMonitoring_parameter());
                //One pmJob per vdu (Actually)
                log.debug("monitoring paramenter are:  "+monitoringParamenters);
                String pmJobId = monitoringPluginCaller.createPMJob(objectSelection,monitoringParamenters,new ArrayList<String>(),10,0);
                vduIdPmJobIdMap.put(vdu.getId(),pmJobId);

                for (VNFFaultManagementPolicy vnffmp : vnfr.getFault_management_policy()){
                    for(Criteria criteria: vnffmp.getCriteria()){
                        String performanceMetric= criteria.getParameter_ref();
                        ThresholdDetails thresholdDetails= new ThresholdDetails("last(0)",criteria.getThreshold(),criteria.getComparison_operator());
                        thresholdDetails.setPerceivedSeverity(vnffmp.getSeverity());
                        String thresholdId = monitoringPluginCaller.createThreshold(objectSelection, performanceMetric, ThresholdType.SINGLE_VALUE, thresholdDetails);
                    }

                }
                log.debug("end vnfPolicyCreator:  ");
            } catch (MonitoringException e) {
                log.error(e.getMessage(),e);
            } catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
    }

    private class VNFFaultMonitor implements Runnable {
        private VNFFaultManagementPolicy vnfFaultManagementPolicy;
        private VirtualDeploymentUnit vdu;

        private Random randomGenerator = new Random();
        private VirtualNetworkFunctionRecord vnfr;

        private Set<String> fakeHostNames;
        private List<String> fakeMetrics;


        public VNFFaultMonitor(VNFFaultManagementPolicy vnfFaultManagementPolicy, VirtualDeploymentUnit vdu){
            if(vnfFaultManagementPolicy==null || vdu==null)
                throw new NullPointerException("The VNFFaultManagementPolicy or the vdus is null");
            this.vnfFaultManagementPolicy=vnfFaultManagementPolicy;
            this.vdu=vdu;

        }

        public void setFakeHostname(Set<String> fakeHostNames) {
            this.fakeHostNames=fakeHostNames;
        }

        public void setFakeZabbixMetrics(List<String> metrics){
            this.fakeMetrics=metrics;
        }

        @Override
        public void run() {

            // Perform a request for the current vdu and get list of Items
            //TEST RANDOM ITEMS
            /*try {

               *//* HttpResponse<String> jsonResponse = null;
                String body = prepareJson();
                jsonResponse = Unirest.put(getUrlToMonitoringApi()).header("Content-type","application/json-rpc").header("KeepAliveTimeout","5000").body(body).asString();
                List<Item> items= getItemsFromJson(jsonResponse.getBody());

                log.debug("Received the following Items: "+ items);
                *//**//*List<Item> randomItems = createRandomItems(fakeHostNames, fakeMetrics);
                log.debug("Created the following random items:" + randomItems);
                //TEST*//**//*
                Map<String,List<Item>> hostnameItems=getMap(items);

                Map<String, Integer> criteriaViolated = new HashMap<>();*//*
                *//*for (Criteria criteria : vnfFaultManagementPolicy.getCriteria()) {
                    log.debug("Fetching criteria:" + criteria);
                    Metric currentMetric = criteria.getParameterRef();
                    MonitoringParameter mp = getMonitoringParameter(vdu,currentMetric);
                    String currentZabbixMetric = null;
                    try {
                        currentZabbixMetric = Zabbix_v2_4_MetricParser.getZabbixMetric(mp.getMetric(), mp.getParams());
                    } catch (ZabbixMetricParserException e) {
                        e.printStackTrace();
                    }
                    log.debug("The current metric is:" + currentZabbixMetric);
                    for (String currentHostname : hostnameItems.keySet()) {
                        for (Item item : hostnameItems.get(currentHostname)) {
                            if (item.getMetric().equals(currentZabbixMetric)) {
                                if (checkThreshold(item, criteria.getThreshold(), criteria.getComparisonOperator())) {
                                    //log.debug("The vnfc: " + item.getHostname() + " has violated the criteria: " + criteria.getName());
                                    if (criteriaViolated.get(item.getHostname()) == null) {
                                        criteriaViolated.put(item.getHostname(), 1);
                                    } else {
                                        criteriaViolated.put(item.getHostname(), criteriaViolated.get(item.getHostname()).intValue() + 1);
                                    }
                                }
                            }
                        }
                    }
                }*//**//*
                for (Map.Entry<String, Integer> entry : criteriaViolated.entrySet()) {
                    if (entry.getValue() == vnfFaultManagementPolicy.getCriteria().size()) {
                        log.debug("The vnfc: " + entry.getKey() + " crossed the threshold of all the criteria");
                        log.debug(entry.toString());
                        log.debug("So the following action need to be executed: " + vnfFaultManagementPolicy.getAction());
                        createAndSendAlarm(vnfFaultManagementPolicy.getSeverity());
                    }
                }

                log.debug("\n\n");*//*

            } catch (ZabbixMetricParserException e) {
                log.error(e.getMessage(), e);
            }catch (UnirestException e) {
                log.error(e.getMessage(),e);
            }catch(Exception e){
                log.error("Thread managing vnffmpolicy: "+vnfFaultManagementPolicy.getName(),e);
            }*/
        }

        private Map<String, List<Item>> getMap(List<Item> items) {
            Map<String,List<Item>> result= new HashMap<>();
            for(Item i : items){
                List<Item> list;
                if(result.get(i.getHostname())==null){
                    list= new ArrayList<>();
                    list.add(i);
                    result.put(i.getHostname(),list);
                }else {
                    list=result.get(i.getHostname());
                    list.add(i);
                    result.put(i.getHostname(),list);
                }
            }
            return result;
        }

        /*private MonitoringParameter getMonitoringParameter(VirtualDeploymentUnit vdu, Metric currentMetric) {
            for(MonitoringParameter mp: vdu.getMonitoring_parameter()){
                if (mp.getMetric().ordinal()== currentMetric.ordinal())
                    return mp;
            }
            throw new NullPointerException("The vdu "+vdu.getId()+" has no monitoring parameter with the metric: "+currentMetric);
        }*/

        private List<Item> getItemsFromJson(String body) {
            List<Item> items=new ArrayList<>();
            /*JsonElement jsonElement = Mapper.getMapper().fromJson(body, JsonElement.class);
            if(!jsonElement.isJsonArray())
                throw new JsonParseException("The json received is not a list of Items");
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for(JsonElement el : jsonArray){
                Item i= Mapper.getMapper().fromJson(el,Item.class);
                items.add(i);
            }*/
            return items;
        }

        private boolean checkThreshold(Item item, String threshold,String comparisionOperator ) {
            boolean result=false;
            float lastValue = Float.parseFloat(item.getLastValue());
            float thresholdValue = Float.parseFloat(threshold);
            switch (comparisionOperator){
                case "=": result= lastValue == thresholdValue;break;
                case ">": result = lastValue > thresholdValue;break;
                case "<": result = lastValue < thresholdValue;break;
                case ">=": result = lastValue >= thresholdValue;break;
                case "<=": result = lastValue <= thresholdValue;break;
                default : log.error("Invalid comparision operator");
            }
            return result;
        }
        /*private String prepareJson() throws ZabbixMetricParserException {
            String apiRest="{ 'keys':[";
            for(MonitoringParameter mp: vdu.getMonitoring_parameter()){
                String metric=Zabbix_v2_4_MetricParser.getZabbixMetric(mp.getMetric(),mp.getParams());
                apiRest+="\""+metric+"\",";
            }
            apiRest=apiRest.substring(0, apiRest.length()-1);
            apiRest+="],'period'='"+0+"'";
            return apiRest;
        }*/
        private void createAndSendAlarm(PerceivedSeverity perceivedSeverity) {
            Alarm alarm = new Alarm();
            DateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            alarm.setEventTime(dateFormat.format(date));

            alarm.setAlarmState(AlarmState.FIRED);
            alarm.setPerceivedSeverity(perceivedSeverity);
            alarm.setFaultType(FaultType.VNF_NOT_AVAILABLE);
            alarm.setResourceId(vnfr.getId());
            alarm.setFaultDetails(vnfFaultManagementPolicy.getName());
            //AbstractVNFAlarm vnfAlarmNotification=new VNFAlarmNotification(this,alarm);


            //publisher.publishEvent(vnfAlarmNotification);
        }

        private List<Item> getItemsFromHostname(List<Item> items,String hostname) {
            List<Item> result= new ArrayList<>();
            for (Item i : items){
                if(i.getHostname().equals(hostname))
                    result.add(i);
            }
            return result;
        }

        private List<Item> createRandomItems(Set<String> hostnames, List<String> metrics) {
            List<Item> items=new ArrayList<>();
            for (int i=0 ; i<metrics.size() ; i++){
                Item item=new Item();
                item.setMetric(metrics.get(i));
                items.add(item);
            }
            List<Item> finalItems= new ArrayList<>();
            for (String hostname: hostnames){
                for(Item item : items){
                    Item newItem= new Item();
                    int lastvalue=randomGenerator.nextInt(10) > 5 ? 0 : 1;
                    newItem.setLastValue(Integer.toString(lastvalue));
                    newItem.setMetric(item.getMetric());
                    newItem.setHostname(hostname);
                    finalItems.add(newItem);
                }
            }
            return finalItems;
        }
        public String getUrlToMonitoringApi(){
            return monitorApiUrl+"/nsr/"+vnfr.getParent_ns_id()+"/vnfr/"+vnfr.getId()+"/vdu/"+vnfr.getVdu().iterator().next().getId();
        }
        public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
            this.vnfr = vnfr;
        }
    }
}
