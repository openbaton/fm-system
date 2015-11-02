package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.Criteria;
import org.openbaton.catalogue.mano.common.faultmanagement.MonitoringParameter;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.nfvo.Item;
import org.openbaton.faultmanagement.exceptions.ZabbixMetricParserException;
import org.openbaton.faultmanagement.parser.Zabbix_v2_4_MetricParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mob on 30.10.15.
 */
public class FaultMonitor implements Runnable{
    private VNFFaultManagementPolicy vnfFaultManagementPolicy;
    private VirtualDeploymentUnitShort vdus;
    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    private Random randomGenerator = new Random();

    private Set<String> fakeHostNames;
    private List<String> fakeMetrics;

    public FaultMonitor(VNFFaultManagementPolicy vnfFaultManagementPolicy, VirtualDeploymentUnitShort vdus){
        this.vnfFaultManagementPolicy=vnfFaultManagementPolicy;
        this.vdus=vdus;
        fakeHostNames=createFakeHostnameForVdus();
    }

    private Set<String> createFakeHostnameForVdus() {
        Set<String> result=new HashSet<>();
        result.add("host1"+vdus.getName());
        result.add("host2"+vdus.getName());
        return result;
    }

    public void setFakeZabbixMetrics(List<String> metrics){
        this.fakeMetrics=metrics;
    }
    @Override
    public void run() {

        //call zabbix plugin and get Items for those hostnames belong to the current vnf

            //Perform a request for the current vdu and get list of Items
            //TEST RANDOM ITEMS
            List<Item> randomItems= createRandomItems(fakeHostNames, fakeMetrics);
            //TEST
            int numCriteriaViolated = 0;
            for (Criteria criteria : vnfFaultManagementPolicy.getCriteria()) {
            MonitoringParameter mp = vdus.getMonitoringParameter(criteria.getParameter_ref());
                String currentMetric=null;
                try {
                    currentMetric = Zabbix_v2_4_MetricParser.getZabbixMetric(mp.getMetric(),mp.getParams());
                } catch (ZabbixMetricParserException e) {
                    e.printStackTrace();
                }
            for (String currentHostname: fakeHostNames)
            for(Item item: randomItems){
                if(item.getMetric().equals(currentMetric)){
                    if(item.getLastValue().equals(criteria.getThreshold())){
                        if(currentHostname.equals(item.getHostname())){
                            log.debug("The vnfc: "+item.getHostname()+" has violated the criteria: "+criteria);
                        }
                    }
                }
            }

            if (numCriteriaViolated == vnfFaultManagementPolicy.getCriteria().size())
                log.debug("All criteria in the policy are violated send alarm!");
        }
    }

    private VirtualDeploymentUnitShort getVdus(String vduSelectedName, List<VirtualDeploymentUnitShort> vdusList) {
        for(VirtualDeploymentUnitShort vdus: vdusList){
            if(vdus.getName().equals(vduSelectedName))
                return vdus;
        }
        return null;
    }
    private void createAndSendAlarm() {

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
            int lastvalue=randomGenerator.nextInt(10) > 7 ? 0 : 1;
            item.setLastValue(Integer.toString(lastvalue));

            item.setMetric(metrics.get(i));
            items.add(item);
        }
        List<Item> finalItems= new ArrayList<>();
        for (String hostname: hostnames){
            for(Item item : items){
                Item newItem= item;
                newItem.setHostname(hostname);
                finalItems.add(newItem);
            }
        }
        return finalItems;
    }
}
