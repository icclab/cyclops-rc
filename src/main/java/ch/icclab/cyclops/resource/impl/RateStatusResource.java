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

import ch.icclab.cyclops.model.RateStatusResponse;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.Flag;
import ch.icclab.cyclops.util.Load;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Author: Srikanta
 * Created on: 27-Mar-15
 * Description:
 *
 */
public class RateStatusResource extends ServerResource {

    // who am I?
    private String endpoint = "/rate/status";

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * Gets the rate of a resource
     *
     * Pseudo Code
     * 1. Check for the rating policy
     * 2. Invoke the methods to construct the response
     *
     * @return Representation
     */
    @Get
    public Representation getRate(){

        counter.increment(endpoint);

        if(Flag.getMeteringType().equalsIgnoreCase("static")){
            // Construct the response
            return buildStaticRateResponse();
        }else{
            // Construct the response
            return buildDynamicRateResponse();
        }
    }

    //TODO: parametrize these two methods and replace it with one
    /**
     * Build the static rate of a resource
     *
     * Pseudo Code
     * 1. Get the latest static rates from a list
     * 2. Construct the response
     * 3. Return the json string
     *
     * @return Representation
     */
    private Representation buildStaticRateResponse() {
        String jsonStr = null;
        RateStatusResponse response = new RateStatusResponse();
        ObjectMapper mapper = new ObjectMapper();
        response.setRate_policy("static");
        response.setRate(Load.getStaticRate());
        try {
            jsonStr = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        JsonRepresentation jsonResp = new JsonRepresentation(jsonStr);
        return jsonResp;
    }

    /**
     * Build the dynamic rate status response
     *
     * Pseudo Code
     * 1. Set the rating policy as dynamic and construct the response
     * 2. Return the response json string
     *
     * @return Representation
     */
    private Representation buildDynamicRateResponse() {
        String jsonStr = null;
        RateStatusResponse response = new RateStatusResponse();
        ObjectMapper mapper = new ObjectMapper();
        response.setRate_policy("dynamic");
        response.setRate(null);
        try {
            jsonStr = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        JsonRepresentation jsonResp = new JsonRepresentation(jsonStr);
        return jsonResp;
    }
}