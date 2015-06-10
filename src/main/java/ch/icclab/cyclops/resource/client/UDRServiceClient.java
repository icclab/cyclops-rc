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

package ch.icclab.cyclops.resource.client;

import ch.icclab.cyclops.model.ResourceUsage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import ch.icclab.cyclops.util.Load;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description: Clinet class for connecting to the UDR service
 *
 */
public class UDRServiceClient extends ClientResource {

    private String url = Load.configuration.get("UDRServiceUrl");

    /**
     * Gets the usage data for a resource
     *
     * Pseudo Code
     * 1. Load the URL of the rate engine
     * 2. Query the rule engine to get the rate for a resource
     * 3. Convert the resonse into a JSON object
     *
     * @param resourceName A string containing the name of the resource
     * @param from Timestamp for the starting date
     * @param to Timestamp for the ending date
     * @return ResourceUsage
     */
    public ResourceUsage getResourceUsageData(String resourceName, String from, String to) throws IOException {
        ResourceUsage resourceUsageData = null;
        JSONObject resultArray;
        ObjectMapper mapper = new ObjectMapper();

        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"/usage/resources/"+resourceName);
        resource.getReference().addQueryParameter("from",from.toString());
        resource.getReference().addQueryParameter("to",to.toString());
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        try {
            resultArray = new JSONObject(output.getText());
            resourceUsageData = mapper.readValue(resultArray.toString(),ResourceUsage.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resourceUsageData;
    }

    /**
     * Gets the selected list of resources
     *
     * Pseudo Code
     * 1. Query the UDR service /meters API
     * 2. Return the JSON string
     *
     * @return String
     */
    public String getActiveResources() throws IOException{
        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"/meters");
        resource.getRequest();
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        String result = output.getText();
        return result;
    }
}
