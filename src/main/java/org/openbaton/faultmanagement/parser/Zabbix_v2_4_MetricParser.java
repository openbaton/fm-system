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
            case NET_TCP_LISTEN: return getNetTcpListen(params);
        }
        return "";
    }

    private static String getNetTcpListen(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.tcp.listen metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.tcp.listen metric requires the parameter p1=[port]");
        return "net.tcp.listen["+params.get("p1")+"]";
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
