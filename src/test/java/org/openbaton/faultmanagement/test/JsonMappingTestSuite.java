package org.openbaton.faultmanagement.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openbaton.catalogue.mano.common.faultmanagement.*;
import org.openbaton.catalogue.mano.common.monitoring.PerceivedSeverity;
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
        json=Utils.getFile("json_file/NetworkServiceDescriptor-iperf-server-vim-18.json");
        assertNotNull(json);
    }

    @Test
    public void testJsonMapping(){
        nsd = mapper.fromJson(json,NetworkServiceDescriptor.class);
        assertEquals("NSD name must be equals",nsd.getName(),"iperf-NS");

    }

    @Test
    public void testFaultManagementPolicy(){

        nsd = mapper.fromJson(json,NetworkServiceDescriptor.class);

        VRFaultManagementPolicy expectedVRFaultManagementPolicy = new VRFaultManagementPolicy();
        expectedVRFaultManagementPolicy.setName("Iper-server-down");
        expectedVRFaultManagementPolicy.setPeriod(5);
        expectedVRFaultManagementPolicy.setSeverity(PerceivedSeverity.CRITICAL);
        expectedVRFaultManagementPolicy.setAction(FaultManagementAction.SWITCH_TO_STANDBY);

        Set<Criteria> criterias = new HashSet<>();
        Criteria c1 = new Criteria();
        c1.setName("Iper-server not listening");
        c1.setComparison_operator("=");
        c1.setParameter_ref("net.tcp.listen[5001]");
        c1.setThreshold("0");

        criterias.add(c1);
        expectedVRFaultManagementPolicy.setCriteria(criterias);

        for(VirtualNetworkFunctionDescriptor vnfd : nsd.getVnfd()){
            if(vnfd.getName().equals("iperf-server")){
                for(VirtualDeploymentUnit vdu : vnfd.getVdu()){
                    for(VRFaultManagementPolicy vnffmp: vdu.getFault_management_policy()){
                        System.out.println("Actual"+vnffmp);
                    }
                    System.out.println("\n\n");
                    //System.out.println("Expect"+ expectedVRFaultManagementPolicy);
                    //assertEquals("VRFaultManagementPolicy name should be the same", expectedVRFaultManagementPolicy.getName(),vnffmp.getName());
                    Iterator<String> it= vdu.getMonitoring_parameter().iterator();
                    while(it.hasNext()){
                        System.out.println("Mon par: "+it.next());
                    }
                }
            }
        }
    }
}
