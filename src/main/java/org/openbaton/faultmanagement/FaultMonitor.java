package org.openbaton.faultmanagement;

import org.openbaton.catalogue.mano.common.faultmanagement.Criteria;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.nfvo.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mob on 30.10.15.
 */
public class FaultMonitor implements Runnable{
    private VNFFaultManagementPolicy vnfFaultManagementPolicy;
    private Map<String,String> hostnamesVduMap;
    private List<String> metrics;
    private static final Logger log = LoggerFactory.getLogger(NSRManager.class);
    Random randomGenerator = new Random();

    public FaultMonitor(VNFFaultManagementPolicy vnfFaultManagementPolicy, Map<String,String> hostnamesVduMap, List<String>metrics){
        this.vnfFaultManagementPolicy=vnfFaultManagementPolicy;
        this.hostnamesVduMap=hostnamesVduMap;
        this.metrics=metrics;
        //vdulist
    }

    @Override
    public void run() {

        //call zabbix plugin and get Items for those hostnames belong to the current vnf

        List<Item> randomItems= createRandomItems(hostnamesVduMap.keySet(), metrics);
        int numCriteriaViolated=0;
        for(Criteria criteria: vnfFaultManagementPolicy.getCriteria()) {
            //call zabbix for vdu id
            for(Item item: randomItems){
                if(item.getMetric().equals(criteria.getParameter_ref())){
                    if(item.getLastValue().equals(criteria.getThreshold()) && criteria.getStatistic().equals("at_least_one")){
                        numCriteriaViolated++;
                        log.debug("The vnfc: "+item.getHostname()+" has violated the criteria: "+criteria);
                    }
                }
            }
        }
        if(numCriteriaViolated == vnfFaultManagementPolicy.getCriteria().size())
            log.debug("All criteria in the policy are violated send alarm!");
    }

    private List<String> getHostnameForVdu(String vdu_selector) {
        List<String> result=new ArrayList<>();
        for(String hostname : hostnamesVduMap.keySet()){
            if(vdu_selector.equals(hostnamesVduMap.get(hostname))){
                result.add(hostname);
            }
        }
        return result;
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
        int numItems=hostnames.size()*metrics.size();
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
