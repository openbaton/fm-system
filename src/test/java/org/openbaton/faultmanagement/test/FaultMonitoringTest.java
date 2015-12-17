package org.openbaton.faultmanagement.test;

import org.junit.Before;
import org.junit.Test;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.faultmanagement.fc.parser.Mapper;

import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;

/**
 * Created by mob on 02.11.15.
 */
public class FaultMonitoringTest {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private NetworkServiceDescriptor nsd;

    @Before
    public void init() {
        String json = Utils.getFile("json_file/NetworkServiceDescriptor-iperf.json");
        assertNotNull(json);
        nsd = Mapper.getMapper().fromJson(json, NetworkServiceDescriptor.class);

    }

    @Test
    public void testFaultMonitor() {
        for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {
            if (vnfd.getName().equals("iperf-server")) {
                //get the first VnfFaultManagementPolicy
                VNFFaultManagementPolicy vnffmp= vnfd.getFault_management_policy().iterator().next();


                //The fault monitor will check every vnffmp.getPeriod() seconds if the VNFCs of that VDU have crossed the thresholds.
                /*VNFFaultMonitor fm=new VNFFaultMonitor(vnffmp,vdus1);
                fm.setFakeZabbixMetrics(Arrays.asList("net.tcp.listen[6161]", "agent.ping"));
                Set<String> set = new HashSet<>(Arrays.asList("host1", "host2","host3"));
                fm.setFakeHostname(set);
                System.out.println("Schedule a fault monitor each "+vnffmp.getPeriod()+" seconds");
                scheduler.scheduleAtFixedRate(fm, 1, vnffmp.getPeriod(), TimeUnit.SECONDS);*/
            }
        }
        try {
            Thread.sleep(1000*40);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shutdownAndAwaitTermination(scheduler);
    }
    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
