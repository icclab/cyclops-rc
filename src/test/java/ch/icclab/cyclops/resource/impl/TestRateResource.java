/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import ch.icclab.cyclops.util.Flag;
import ch.icclab.cyclops.util.Load;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Srikanta
 * Created on: 21-Jun-15
 * Description: The test class for unit testing the methods of RateResource
 */
public class TestRateResource {
    private InfluxDBClient mockedDbClient;
    private RateResource rateResource;

    @Before
    public void prepareMocks(){
        mockedDbClient = mock(InfluxDBClient.class);
        Load.configuration = new HashMap<String, String>();

        // Load the static config hashmap
        Load.configuration.put("InfluxDbUrl","http://localhost:8083/db/udr_service/series");
        Load.configuration.put("InfluxDBUsername","username");
        Load.configuration.put("InfluxDBPassword","password");
    }

    @Test
    public void testGetRate() throws IOException, JSONException {
        ArrayList<String> columnNames = new ArrayList<String>();
        ArrayList<Object> pointsData = new ArrayList<Object>();
        ArrayList<ArrayList<Object>> points = new ArrayList<ArrayList<Object>>();

        TSDBData stubbedTsdbData = new TSDBData();

        String time = "1433908721008";
        String seq_numb = "362977270001";
        String rate = "2";

        String jsonStr = "{\"time\":{\"to\":\"2015-06-10 03:59\",\"from\":\"2015-06-10 00:00\"},\"rate\":{\"cpu\":[[\"1433908721008\",\"362977270001\",\"2\"]]}}";
        JsonRepresentation jsonResponse = new JsonRepresentation(jsonStr);

        // Prepatation of the stubbed tsdb data.
        columnNames.add("time");
        columnNames.add("sequence_number");
        columnNames.add("rate");
        pointsData.add(time);
        pointsData.add(seq_numb);
        pointsData.add(rate);
        points.add(pointsData);

        // Populating the stubbed tsdb data
        stubbedTsdbData.setName("rate");
        stubbedTsdbData.setColumns(columnNames);
        stubbedTsdbData.setPoints(points);

        // Preparing the mocking condition & response for influxdb client
        when(mockedDbClient.getData("SELECT time,rate FROM rate WHERE resource='cpu' AND time > '2015-06-10 00:00' AND time < '2015-06-10 03:59'")).thenReturn(stubbedTsdbData);

        // Invoking the class and the method for testing
        rateResource = new RateResource(mockedDbClient,"cpu","2015-06-10 00:00","2015-06-10 03:59");

        // Assertion of the generated and expected response
        JSONAssert.assertEquals(jsonResponse.getText(), rateResource.getRate().getText(), true);
    }

    @Test
    public void testSetDynamicRate(){
        Representation entity;
        String jsonStr;
        rateResource = new RateResource();

        // Create a JSON string which acts as a body of the POST request
        jsonStr = "{\"source\":\"dashboard\",\"time\":\"2015-03-17 17:34:45\",\"rate_policy\":\"dynamic\",\"rate\":null}";
        entity = new JsonRepresentation(jsonStr);

        // Invoke the class and pass the json string
        rateResource.setRate(entity);

        // Assert if the flag is set to the appropriate string
        assertEquals("The Rate Policy flag was not set to dynamic", "dynamic", Flag.getMeteringType());
    }

    @Test
    public void testSetStaticRate(){
        String data ="{\"source\":\"dashboard\",\"time\":\"2015-03-17 17:34:45\",\"rate_policy\":\"static\",\"rate\":{\"cpu\":0.00004}}";
        JsonRepresentation jsonStr = new JsonRepresentation(data);

        // Prepare the mock response of the dbClient
        when(mockedDbClient.saveData("{\"name\":\"rate\",\"columns\":[\"resource\",\"rate\",\"rate_policy\"],\"points\":[[\"cpu\",4.0E-5,\"static\"]]}")).thenReturn(true);

        // Instantiate the class being tested
        rateResource = new RateResource(mockedDbClient);

        // Invoke the method being tested
        rateResource.setRate(jsonStr);
        }
}