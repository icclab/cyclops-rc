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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import ch.icclab.cyclops.util.Load;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Konstantin
 * Created on: 30-Sep-15
 * Description: Client class for connecting to the T-Nova Accounting
 *
 * This class connects to T-Nova Accounting module (part of marketplace WP6) via its API.
 * It gets the VNF instances associated to a service instance, extracts the underlying
 * billing model and computes the charge per VNF.
 */

public class AccountingClient extends ClientResource {
    final static Logger logger = LogManager.getLogger(AccountingClient.class.getName());

    private String url = Load.configuration.get("AccountingServiceUrl");

    /**
     * Gets the usage data for a resource
     *
     * Pseudo Code
     * 1. Load the URL of the rate engine
     * 2. Query the rule engine to get the rate for a resource
     * 3. Convert the resonse into a JSON object
     *
     * @param service_instance_id A string containing the name of the T-Nova service instance id
     * @return ArrayList<String>
     */
    public ArrayList<String> getAssociatedVnfs(String service_instance_id) throws IOException {
        logger.trace("BEGIN ResourceUsage getResourceUsageData(String resourceName, String from, String to) throws IOException");
       // logger.trace("DATA ResourceUsage getResourceUsageData...: resourceName="+resourceName);
        ResourceUsage resourceUsageData = null;
        JSONObject resultArray;
        ObjectMapper mapper = new ObjectMapper();

        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"/usage/resources/");
       // resource.getReference().addQueryParameter("from", from.toString());
        //resource.getReference().addQueryParameter("to", to.toString());
        //logger.trace("DATA ResourceUsage getResourceUsageData...: url="+ url + "/usage/resources/"+resourceName + "?from=\"" + from.toString() + "\"&to=\""+ to.toString() + "\"");
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        //Tag tag = output.getTag();
        //logger.trace("DATA ResourceUsage getResourceUsageData...: output="+output.getText());//{"resourceid":"network.outgoing.bytes","time":{"from":"2015-09-24 04:35:12","to":"2015-09-24 09:35:12"},"column":["time","sum"],"usage":[["2015-09-24T04:35:12.000000001Z",608387]]}

        try {
            resultArray = new JSONObject(output.getText());
            logger.trace("DATA ResourceUsage getResourceUsageData...: output="+resultArray.toString());
            logger.trace("DATA ResourceUsage getResourceUsageData...: resultArray="+resultArray);
            resourceUsageData = mapper.readValue(resultArray.toString(),ResourceUsage.class);
            logger.trace("DATA ResourceUsage getResourceUsageData...: resourceUsageData="+resourceUsageData);
        } catch (JSONException e) {
            logger.error("EXCEPTION JSONEXCEPTION ResourceUsage getResourceUsageData...");
            e.printStackTrace();
        }
        logger.trace("DATA ResourceUsage getResourceUsageData...: resourceUsageData="+resourceUsageData);
        logger.trace("END ResourceUsage getResourceUsageData(String resourceName, String from, String to) throws IOException");
        return new ArrayList<String>();
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
        logger.trace("BEGIN String getActiveResources() throws IOException");
        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"/meters");
        resource.getRequest();
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        String result = output.getText();
        logger.trace("DATA String getActiveResources() throws IOException: output="+result);
        logger.trace("END String getActiveResources() throws IOException");
        return result;
    }
}
