package org.openbaton.faultmanagement.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openbaton.catalogue.mano.common.faultmanagement.VNFFaultManagementPolicy;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.faultmanagement.Application;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.openbaton.faultmanagement.parser.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Array;
import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * Created by mob on 02.11.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class PolicyManagerTest {

    private NetworkServiceDescriptor nsd;
    private Gson mapper=new GsonBuilder().create();
    @Autowired
    PolicyManager policyManager;

    @Before
    public void init() {
        String json = Utils.getFile("json_file/NetworkServiceDescriptor-iperf.json");
        assertNotNull(json);
        nsd = Mapper.getMapper().fromJson(json, NetworkServiceDescriptor.class);
    }

    @Test
    public void policyManagerNotNull(){assertNotNull(policyManager);}

    @Test
    public void manageNSRTest(){
        String url="http://localhost:8080/api/v1/ns-records";
        HttpResponse<JsonNode> jsonResponse=null;
        try {
            jsonResponse = Unirest.get(url).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        Class<?> aClass = Array.newInstance(NetworkServiceRecord.class, 3).getClass();

        NetworkServiceRecord[] nsrArray = (NetworkServiceRecord[]) mapper.fromJson(jsonResponse.getBody().toString(), aClass);

        /*for(NetworkServiceRecord nsr : nsrArray){
           System.out.println("Nsr name: "+nsr.getName() +" nsr id:"+nsr.getId());
            for(VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()){
                for(VNFFaultManagementPolicy fmp: vnfr.getFault_management_policy()){
                    System.out.println("fmpolicy: "+fmp);
                }
            }
        }*/
    }
}
