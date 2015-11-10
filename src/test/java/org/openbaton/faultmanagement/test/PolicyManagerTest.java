package org.openbaton.faultmanagement.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbaton.catalogue.mano.common.faultmanagement.FaultManagementPolicy;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.managers.VnfFaultMonitor;
import org.openbaton.faultmanagement.managers.VnfFaultMonitorImpl;
import org.openbaton.faultmanagement.managers.PolicyManagerImpl;
import org.openbaton.faultmanagement.events.EventDispatcher;
import org.openbaton.faultmanagement.exceptions.FaultManagementPolicyException;
import org.openbaton.faultmanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.parser.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Created by mob on 02.11.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PolicyManagerTest {

    @Configuration
    static class PolicyManagerTestTestContextConfiguration {
        @Bean
        public PolicyManager policyManager() {
            return new PolicyManagerImpl();
        }
        @Bean
        public VnfFaultMonitor faultMonitor(){
            return new VnfFaultMonitorImpl();
        }

        @Bean
        public EventDispatcher eventDispatcher(){
            return new EventDispatcher();
        }
    }

    @Autowired
    private PolicyManager policyManager;

    private NetworkServiceDescriptor nsd;
    private Random randomGenerator;

    @Before
    public void init() {
        String json = Utils.getFile("json_file/NetworkServiceDescriptor-iperf.json");
        assertNotNull(json);
        nsd = Mapper.getMapper().fromJson(json, NetworkServiceDescriptor.class);
        randomGenerator = new Random();
    }

    @Test
    public void policyManagerNotNull(){assertNotNull(policyManager);}

    @Test
    public void manageNSRTest(){
        NetworkServiceRecord nsr = new NetworkServiceRecord();
        nsr.setName(nsd.getName());
        nsr.setId("1");
        nsr.setFaultManagementPolicy(new HashSet<FaultManagementPolicy>());
        Set<VirtualNetworkFunctionRecord> listVnfr=new HashSet<>();
        for(VirtualNetworkFunctionDescriptor vnfd: nsd.getVnfd()){
            VirtualNetworkFunctionRecord vnfr=new VirtualNetworkFunctionRecord();
            vnfr.setName(vnfd.getName());
            vnfr.setId("vnfr" + Integer.toString(randomGenerator.nextInt(30)));
            vnfr.setFaultManagementPolicy(new HashSet<VNFFaultManagementPolicy>());
            if(vnfd.getFault_management_policy()!=null)
                vnfr.setFaultManagementPolicy(vnfd.getFault_management_policy());
            for(VirtualDeploymentUnit vdu: vnfd.getVdu()){
                vdu.setId("vdu"+Integer.toString(randomGenerator.nextInt(30)));
                vdu.setName("vdu-name"+Integer.toString(randomGenerator.nextInt(30)));
            }
            vnfr.setVdu(vnfd.getVdu());
            listVnfr.add(vnfr);
        }
        nsr.setVnfr(listVnfr);
        try {
            policyManager.manageNSR(nsr);
        } catch (FaultManagementPolicyException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000*30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Unmanaging nsr");
        policyManager.unManageNSR(nsr.getId());

        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
