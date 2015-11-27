package org.openbaton.faultmanagement.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mob on 30.10.15.
 */
public class JsonMappingTestSuite {
    private String json;
    private Gson mapper=new GsonBuilder().create();
    private NetworkServiceDescriptor nsd;
    @Before
    public void init(){
        json=Utils.getFile("json_file/NetworkServiceDescriptor-iperf.json");
        assertNotNull(json);
    }

    @Test
    public void testJsonMapping(){
        nsd = mapper.fromJson(json,NetworkServiceDescriptor.class);
        assertEquals("NSD name must be equals",nsd.getName(),"iperf-NSD");

    }

    @Test
    public void testFaultManagementPolicy(){

        nsd = mapper.fromJson(json,NetworkServiceDescriptor.class);

        VNFFaultManagementPolicy expectedVnfFaultManagementPolicy=new VNFFaultManagementPolicy();
        expectedVnfFaultManagementPolicy.setName("vnf-not-available");
        expectedVnfFaultManagementPolicy.setCooldown(10);
        expectedVnfFaultManagementPolicy.setPeriod(30);
        expectedVnfFaultManagementPolicy.setSeverity(PerceivedSeverity.CRITICAL);
        expectedVnfFaultManagementPolicy.setAction(FaultManagementVNFCAction.REINSTANTIATE_SERVICE);

        Set<Criteria> criterias = new HashSet<>();
        Criteria c1 = new Criteria();
        c1.setName("criteria1");
        c1.setComparisonOperator("=");
        c1.setParameterRef(Metric.NET_TCP_LISTEN);
        c1.setThreshold("0");

        Criteria c2 = new Criteria();
        c2.setName("criteria2");
        c2.setComparisonOperator("=");
        c2.setParameterRef(Metric.AGENT_PING);
        c2.setThreshold("0");

        criterias.add(c1);
        criterias.add(c2);
        expectedVnfFaultManagementPolicy.setCriteria(criterias);

        for(VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()){
            if(vnfd.getName().equals("iperf-server")){
                VNFFaultManagementPolicy vnffmp= vnfd.getFault_management_policy().iterator().next();
                System.out.println("Actual"+vnffmp);
                System.out.println("Expect"+expectedVnfFaultManagementPolicy);
                assertEquals("VNFFaultManagementPolicy name should be the same",expectedVnfFaultManagementPolicy.getName(),vnffmp.getName());

                for (VirtualDeploymentUnit vdu : vnfd.getVdu()){
                    Iterator<MonitoringParameter> it= vdu.getMonitoring_parameter().iterator();
                   while(it.hasNext()){
                        System.out.println(it.next().toString());
                    }
                }
            }

        }
    }
}