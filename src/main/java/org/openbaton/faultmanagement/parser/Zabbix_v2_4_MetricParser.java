package org.openbaton.faultmanagement.parser;

import org.openbaton.catalogue.mano.common.faultmanagement.Metric;

import java.util.Map;

/**
 * Created by mob on 28.10.15.
 */
public class Zabbix_v2_4_MetricParser {

    /*public static String getZabbixMetric(Metric metric, Map<String, String> params) throws ZabbixMetricParserException {
        switch (metric) {
            case AGENT_PING:
                return getAgentPing();
            case NET_DNS:
                return getNetDns(params);
            case NET_IF_COLLISION:
                return getNetIfCollision(params);
            case NET_IF_IN:
                return getNetIfIn(params);
            case NET_IF_OUT:
                return getNetIfOut(params);
            case NET_IF_TOTAL:
                return getNetIfTotal(params);
            case NET_TCP_LISTEN:
                return getNetTcpListen(params);
            case NET_TCP_PORT:
                return getNetTcpPort(params);
            case NET_TCP_SERVICE:
                return getTcpService(params);
            case NET_TCP_SERVICE_PERF:
                return getNetTcpServicePerf(params);
            case NET_UDP_LISTEN:
                return getNetUdpListen(params);
            case PROC_MEM:
                return getProcMem(params);
            case PROC_NUM:
                return getProcNum(params);
            case SENSOR:
                return getSensor(params);
            case SYSTEM_CPU_INTR:
                return getSystemCpuIntr();
            case SYSTEM_CPU_LOAD:
                return getSystemCpuLoad(params);
            case SYSTEM_CPU_NUM:
                return getSystemCpuNum(params);
            case SYSTEM_CPU_SWITCHES:
                return getSystemCpuSwitches();
            case SYSTEM_CPU_UTIL:
                return getSystemCpuUtil(params);
            case SYSTEM_STAT:
                return getSystemStat(params);
            case SYSTEM_SWAP_IN:
                return getSystemSwapIn(params);
            case SYSTEM_SWAP_OUT:
                return getSystemSwapOut(params);
            case SYSTEM_SWAP_SIZE:
                return getSystemSwapSize(params);
            case SYSTEM_UPTIME:
                return getSystemUptime();
            case SYSTEM_USERS_NUM:
                return getSystemUserNum();
            case VFS_DEV_READ:
                return getVfsDevRead(params);
            case VFS_DEV_WRITE:
                return getVfsDevWrite(params);
            case VFS_FILE_CKSUM:
                return getVfsFileCksum(params);
            case VFS_FILE_EXISTS:
                return getVfsFileExists(params);
            case VFS_FILE_REGMATCH:
                return getVfsFileRegmatch(params);
            case VFS_FILE_SIZE:
                return getVfsFileSize(params);
            case VFS_FS_INODE:
                return getVfsFsInode(params);
            case VFS_FS_SIZE:
                return getVfsFsSize(params);
            case VM_MEMORY_SIZE:
                return getVmMemorySize(params);
            case WEB_PAGE_PERF:
                return getWebPagePerf(params);
            default : throw new ZabbixMetricParserException("The metric is unknown");
        }
    }

    private static String getWebPagePerf(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("web.page.perf metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("web.page.perf metric requires the parameter p1=[hostname]");
        String result="web.page.perf["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        result+= params.get("p3")==null ? ",": params.get("p3");
        return result+"]";
    }

    private static String getVmMemorySize(Map<String, String> params) {
        if(params==null) return "vm.memory.size";
        return "vm.memory.size["+(params.get("p1")==null?",":params.get("p1"))+"]";
    }

    private static String getVfsFsSize(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("vfs.fs.size metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("vfs.fs.size metric requires the parameter p1=[filesystem]");
        String result = "vfs.fs.inode["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getVfsFsInode(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("vfs.fs.inode metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("vfs.fs.inode metric requires the parameter p1=[filesystem]");
        String result = "vfs.fs.inode["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getVfsFileSize(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("vfs.file.size metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("vfs.file.size metric requires the parameter p1=[full path to file]");
        return "vfs.file.size["+params.get("p1")+"]";
    }

    private static String getVfsFileRegmatch(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("vfs.file.regmatch metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("vfs.file.regmatch metric requires the parameter p1=[full path to file]");
        if(params.get("p2")==null) throw new ZabbixMetricParserException("vfs.file.regmatch metric requires the parameter p2=[GNU regular expression]");
        String result = "vfs.file.regmatch[";
        for(int i=1 ; i<6 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getVfsFileExists(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("vfs.file.exists metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("vfs.file.exists metric requires the parameter p1=[file]");
        return "vfs.file.exists["+params.get("p1")+"]";
    }

    private static String getVfsFileCksum(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("vfs.file.cksum metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("vfs.file.cksum metric requires the parameter p1=[file]");
        return "vfs.file.cksum["+params.get("p1")+"]";
    }

    private static String getVfsDevWrite(Map<String, String> params) {
        if(params==null) return "vfs.dev.write";
        String result = "vfs.dev.write[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        result+= params.get("p3")==null ? ",": params.get("p3");
        return result+"]";
    }

    private static String getVfsDevRead(Map<String, String> params) {
        if(params==null) return "vfs.dev.read";
        String result = "vfs.dev.read[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        result+= params.get("p3")==null ? ",": params.get("p3");
        return result+"]";
    }

    private static String getSystemSwapSize(Map<String, String> params) {
        if(params==null) return "system.swap.size";
        String result = "system.swap.size[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getSystemSwapOut(Map<String, String> params) {
        if(params==null) return "system.swap.out";
        String result = "system.swap.out[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getSystemSwapIn(Map<String, String> params) {
        if(params==null) return "system.swap.in";
        String result = "system.swap.in[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getSystemStat(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("system.stat metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("sensor metric requires the parameter p1=[resource]");
        String result = "system.stat["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getSystemCpuUtil(Map<String, String> params) {
        if(params==null) return "system.cpu.util";
        String result = "system.cpu.util[";
        for(int i=1 ; i<4 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getSystemCpuNum(Map<String, String> params) {
        if(params==null) return "system.cpu.num";
        String result = "system.cpu.num[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        return result+"]";
    }

    private static String getSystemCpuLoad(Map<String, String> params) {
        if(params==null) return "system.cpu.load";
        String result = "system.cpu.load[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getSensor(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("sensor metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("sensor metric requires the parameter p1=[device name]");
        if(params.get("p2")==null) throw new ZabbixMetricParserException("sensor metric requires the parameter p2=[sensor name]");
        String result = "sensor[";
        for(int i=1 ; i<4 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getProcNum(Map<String, String> params) {
        if(params==null) return "proc.num";
        String result = "proc.num[";
        for(int i=1 ; i<5 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getProcMem(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) return "proc.mem";
        String result = "proc.mem[";
        for(int i=1 ; i<5 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getNetUdpListen(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.udp.listen metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.udp.listen metric requires the parameter p1=[port]");
        return "net.udp.listen["+params.get("p1")+"]";
    }

    private static String getNetTcpServicePerf(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.tcp.service.perf metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.tcp.service.perf metric requires the parameter p1=[service]");
        String result = "net.tcp.service.perf[";
        for(int i=1 ; i<4 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getTcpService(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.tcp.service metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.tcp.service metric requires the parameter p1=[service]");
        String result = "net.tcp.service[";
        for(int i=1 ; i<4 ;i++){
            result+= params.get("p"+i)==null ? ",": params.get("p"+i);
        }
        return result+"]";
    }

    private static String getNetTcpPort(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.tcp.port metric has null parameters");
        if(params.get("p2")==null) throw new ZabbixMetricParserException("net.tcp.port metric requires the parameter p2=[port]");
        String result = "net.tcp.port[";
        result+= params.get("p1")==null ? ",": params.get("p1");
        return result+params.get("p2")+"]";
    }

    private static String getNetIfTotal(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.if.total metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.if.total metric requires the parameter p1=[network interface name]");
        String result = "net.if.total["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getNetIfOut(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.if.out metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.if.out metric requires the parameter p1=[network interface name]");
        String result = "net.if.out["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getNetIfIn(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.if.in metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.if.in metric requires the parameter p1=[network interface name]");
        String result = "net.if.in["+params.get("p1");
        result+= params.get("p2")==null ? ",": params.get("p2");
        return result+"]";
    }

    private static String getNetIfCollision(Map<String, String> params) throws ZabbixMetricParserException {
        if(params==null) throw new ZabbixMetricParserException("net.if.collision metric has null parameters");
        if(params.get("p1")==null) throw new ZabbixMetricParserException("net.if.collision metric requires the parameter p1=[network interface name]");
        return "net.if.collision["+params.get("p1")+"]";
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

    public static String getSystemCpuIntr() {
        return "system.cpu.intr";
    }

    public static String getSystemCpuSwitches() {
        return "system.cpu.switches";
    }

    public static String getSystemUptime() {
        return "system.uptime";
    }

    public static String getSystemUserNum() {
        return "system.user.num";
    }*/
}
