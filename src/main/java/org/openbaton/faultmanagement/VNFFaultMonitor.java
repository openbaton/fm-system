package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.faultmanagement.exceptions.ZabbixMetricParserException;
import org.openbaton.faultmanagement.parser.Zabbix_v2_4_MetricParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mob on 30.10.15.
 */
public class VNFFaultMonitor implements Runnable{
    private VNFFaultManagementPolicy vnfFaultManagementPolicy;
    private VirtualDeploymentUnitShort vdus;
    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    private Random randomGenerator = new Random();

    private Set<String> fakeHostNames;
    private List<String> fakeMetrics;

    public VNFFaultMonitor(VNFFaultManagementPolicy vnfFaultManagementPolicy, VirtualDeploymentUnitShort vdus){
        if(vnfFaultManagementPolicy==null || vdus==null)
            throw new NullPointerException("The VNFFaultManagementPolicy or the vdus is null");
        this.vnfFaultManagementPolicy=vnfFaultManagementPolicy;
        this.vdus=vdus;
    }

    public void setFakeHostname(Set<String> fakeHostNames) {
        this.fakeHostNames=fakeHostNames;
    }

    public void setFakeZabbixMetrics(List<String> metrics){
        this.fakeMetrics=metrics;
    }

    @Override
    public void run() {

        //call zabbix plugin and get Items for those hostnames belong to the current vnf
        // Perform a request for the current vdu and get list of Items
        //TEST RANDOM ITEMS
        List<Item> randomItems = createRandomItems(fakeHostNames, fakeMetrics);
        log.debug("Created the following random items:" + randomItems);
        //TEST
        Map<String,Integer> criteriaViolated=new HashMap<>();
        for (Criteria criteria : vnfFaultManagementPolicy.getCriteria()) {
            log.debug("Fetching criteria:" + criteria);
            MonitoringParameter mp = vdus.getMonitoringParameter(criteria.getParameterRef());
            String currentMetric = null;
            try {
                currentMetric = Zabbix_v2_4_MetricParser.getZabbixMetric(mp.getMetric(), mp.getParams());
            } catch (ZabbixMetricParserException e) {
                e.printStackTrace();
            }
            log.debug("The current metric is:" + currentMetric);
            for (String currentHostname : fakeHostNames) {
                for (Item item : getItemsFromHostname(randomItems,currentHostname)) {
                    if (item.getMetric().equals(currentMetric)) {
                            if (checkThreshold(item, criteria.getThreshold(),criteria.getComparisonOperator())) {
                                //log.debug("The vnfc: " + item.getHostname() + " has violated the criteria: " + criteria.getName());
                                if (criteriaViolated.get(item.getHostname()) == null) {
                                    criteriaViolated.put(item.getHostname(), 1);
                                }
                                else {
                                    criteriaViolated.put(item.getHostname(), criteriaViolated.get(item.getHostname()).intValue()+1);
                                }
                            }
                    }
                }
            }
        }
        for(Map.Entry<String,Integer> entry: criteriaViolated.entrySet()) {
            if (entry.getValue() == vnfFaultManagementPolicy.getCriteria().size()) {
                log.debug("The vnfc: " + entry.getKey() + " crossed the threshold of all the criteria");
                log.debug(entry.toString());
                log.debug("So the following action need to be executed: " + vnfFaultManagementPolicy.getAction());
                createAndSendAlarm(vnfFaultManagementPolicy.getSeverity());
            }
        }

        log.debug("\n\n");
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

    private VirtualDeploymentUnitShort getVdus(String vduSelectedName, List<VirtualDeploymentUnitShort> vdusList) {
        for(VirtualDeploymentUnitShort vdus: vdusList){
            if(vdus.getName().equals(vduSelectedName))
                return vdus;
        }
        return null;
    }
    private void createAndSendAlarm(PerceivedSeverity perceivedSeverity) {
        Alarm alarm = new Alarm();
        DateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        alarm.setEventTime(dateFormat.format(date));

        alarm.setAlarmState(AlarmState.FIRED);
        alarm.setPerceivedSeverity(perceivedSeverity);
        alarm.setFaultType(FaultType.VNF_NOT_AVAILABLE);

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
}
