package org.openbaton.faultmanagement.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mob on 30.10.15.
 */
public class JsonMappingTestSuite {
    private String json;
    private Gson mapper=new GsonBuilder().create();

    @Before
    public void init(){
        json=new JsonMappingTestSuite().getFile("/json_file/NetworkServiceDescriptor-iperf.json");
        assertNotNull(json);
    }

    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }


    @Test
    public void testJsonMapping(){
        NetworkServiceDescriptor nsd = mapper.fromJson(json,NetworkServiceDescriptor.class);
        assertEquals("NSD name must be equals",nsd.getName(),"iperf-NSD");
    }
}
