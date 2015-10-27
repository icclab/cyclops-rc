package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.resource.client.InfluxDBClient;
import ch.icclab.cyclops.resource.client.UDRServiceClient;
import ch.icclab.cyclops.util.Flag;
import ch.icclab.cyclops.util.Load;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Srikanta
 * Created on: 25-Jun-15
 * Description: Tests the methods of the class GenerateResource which implements the logic of generation of
 * Charge Data Records and rates for cloud resources.
 */
public class TestGenerateResource {
    private InfluxDBClient mockDbClient;
    private GenerateResource generateResource;
    private UDRServiceClient mockUdrServiceClient;

    @Before
    public void prepareMock(){
        mockDbClient = mock(InfluxDBClient.class);
        mockUdrServiceClient = mock(UDRServiceClient.class);
    }

    @Test
    public void testRateGeneration() throws IOException, JSONException {
        String jsonData = "{\"name\":\"rate\",\"columns\":[\"resource\",\"rate\",\"rate_policy\"],\"points\":[[\"cpu\",3,\"static\"],[\"network.outgoing.bytes\",1,\"static\"]]}";
        HashMap rateMap = new HashMap();
        Load load = new Load();

        // Set the static rates
        rateMap.put("network.outgoing.bytes", 1);
        rateMap.put("cpu_util", 3);
        load.setStaticRate(rateMap);

        // Set the metering flag to 'static'
        Flag.setMeteringType("static");
        // Prepare the stub for udrServiceClient call
        when(mockUdrServiceClient.getActiveResources()).
                thenReturn("{\"name\":\"meterselection\",\"columns\":[\"time\",\"metersource\",\"metertype\",\"source\",\"status\",\"metername\",\"value\"],\"points\":[[\"2015-09-22T12:44:41.713710586Z\",\"openstack\",\"cumulative\",\"cyclops-ui\",1,\"network.outgoing.bytes\",0],[\"2015-09-22T12:44:49.418251797Z\",\"openstack\",\"gauge\",\"cyclops-ui\",1,\"cpu_util\",0]],\"tags\":null}");
        // Prepare the stub for dbClient call
        when(mockDbClient.saveData(jsonData)).thenReturn(true);
        // Invoke the class being tested
        generateResource = new GenerateResource("rate", mockDbClient, mockUdrServiceClient);
        // Invoke and assert of the output the method under test
        assertEquals("The generated rate request is wrong", generateResource.serviceRequest(), "The rate generation was successful");
    }

    public void testCdrRegeration(){

    }
}
