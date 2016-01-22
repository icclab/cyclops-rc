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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.RateEngineResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 19-May-15
 * Description: Client class for connecting to the rule engine
 */
public class RuleEngineClient extends ClientResource {

    /**
     * Gets the rate for a particular resource
     * <p/>
     * Pseudo Code
     * 1. Load the URL of the rate engine
     * 2. Query the rule engine to get the rate for a resource
     * 3. Convert the resonse into a JSON object
     *
     * @param resourceName A string containing the name of the resource
     * @return RateEngineResponse
     */
    public RateEngineResponse getRate(String resourceName) {
        JsonRepresentation jsonRepresentation = null;
        RateEngineResponse responseObj = null;
        ObjectMapper mapper = new ObjectMapper();
        String url = Loader.getSettings().getCyclopsSettings().getRuleEngine();

        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url + "rate/" + resourceName);
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        try {
            System.out.println(output.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jsonRepresentation = new JsonRepresentation(output.getText());
            System.out.println(jsonRepresentation.getText());
            double rate = (Double) jsonRepresentation.getJsonObject().get("rate");
            System.out.println(rate);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            //JSONObject jsonStr = new JSONObject(output);
            responseObj = mapper.readValue(jsonRepresentation.getJsonObject().toString(), RateEngineResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return responseObj;
    }
}
