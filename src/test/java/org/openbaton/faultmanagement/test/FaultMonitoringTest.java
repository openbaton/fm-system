package org.openbaton.faultmanagement.test;

import org.junit.Before;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.faultmanagement.FaultMonitor;
import org.openbaton.faultmanagement.VirtualDeploymentUnitShort;
import org.openbaton.faultmanagement.parser.Mapper;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * Created by mob on 02.11.15.
 */
public class FaultMonitoringTest {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private NetworkServiceDescriptor nsd;

    @Before
    public void init() {
        String json = Utils.getFile("json_file/NetworkServiceDescriptor-iperf.json");
        assertNotNull(json);
        nsd = Mapper.getMapper().fromJson(json, NetworkServiceDescriptor.class);
    }

    public void testFaultMonitor() {
        for (VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()) {
            if (vnfd.getName().equals("iperf-server")) {
                //get the first VnfFaultManagementPolicy
                VNFFaultManagementPolicy vnffmp= vnfd.getFault_management_policy().iterator().next();
                VirtualDeploymentUnitShort vdus1=getVDUS(vnfd.getVdu().iterator().next());

                //The fault monitor will check every vnffmp.getPeriod() seconds if the VNFCs of that VDU have crossed the thresholds.
                FaultMonitor fm=new FaultMonitor(vnffmp,vdus1);
                fm.setFakeZabbixMetrics(Arrays.asList("net.tcp.listen[6161]", "agent.ping"));
                scheduler.schedule(fm,vnffmp.getPeriod(), TimeUnit.SECONDS);
            }
        }
    }

    private VirtualDeploymentUnitShort getVDUS(VirtualDeploymentUnit next) {
        VirtualDeploymentUnitShort vdus=new VirtualDeploymentUnitShort("FakeID","vdu1");
        vdus.setMonitoringParameters(next.getMonitoring_parameter());
        return vdus;
    }
}
