package org.openbaton.faultmanagement.parser;

import org.openbaton.catalogue.mano.common.faultmanagement.Metric;
import org.openbaton.faultmanagement.exceptions.ZabbixMetricParserException;

import java.util.Map;

/**
 * Created by mob on 28.10.15.
 */
public class Zabbix_v2_4_MetricParser {
    public static String getZabbixMetric(Metric metric,Map<String,String> params) throws ZabbixMetricParserException {
        switch (metric) {
            case AGENT_PING: return getAgentPing();
            case NET_DNS: return getNetDns(params);
        }
        return "";
    }

    private static String getNetDns(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.dns metric has null parameters");
        String result = "net.dns[";
        for(int i=1 ; i<6 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }
    private static String getAgentPing() {
        return "agent.ping";
    }
}
