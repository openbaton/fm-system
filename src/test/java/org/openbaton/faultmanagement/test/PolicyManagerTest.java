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
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.faultmanagement.Application;
import org.openbaton.faultmanagement.fc.interfaces.NSRManager;
import org.openbaton.faultmanagement.fc.parser.Mapper;
import org.openbaton.faultmanagement.fc.policymanagement.interfaces.PolicyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Array;

import static org.junit.Assert.assertNotNull;

/**
 * Created by mob on 02.11.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class,PolicyManager.class,NSRManager.class})
public class PolicyManagerTest {

    private NetworkServiceDescriptor nsd;
    private Gson mapper=new GsonBuilder().create();
    @Autowired
    PolicyManager policyManager;
    @Autowired
    NSRManager nsrManager;

    @Before
    public void init() {
        //String json = Utils.getFile("json_file/NetworkServiceDescriptor-iperf.json");
        //assertNotNull(json);
        //nsd = Mapper.getMapper().fromJson(json, NetworkServiceDescriptor.class);
    }

    @Test
    public void policyManagerNotNull(){
        assertNotNull(policyManager);
        assertNotNull(nsrManager);
    }

    @Test
    public void manageNSRTest(){

        NetworkServiceRecord nsr = nsrManager.getNetworkServiceRecord("e994a41d-de77-4bf4-8438-fd966796d3ad");
        policyManager.manageNSR(nsr);

    }
}
