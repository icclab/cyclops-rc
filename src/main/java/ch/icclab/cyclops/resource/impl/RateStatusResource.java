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
import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import util.Flag;
import util.Load;

/**
 * Author: Srikanta
 * Created on: 27-Mar-15
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class RateStatusResource extends ServerResource {

    @Get
    public Representation getRate(){
        TSDBData tsdbData;
        Long epoch;
        InfluxDBClient dbClient = new InfluxDBClient();

        if(Flag.getMeteringType().equalsIgnoreCase("static")){
            //Get the first entry
            tsdbData = dbClient.getData("select * from rate WHERE rate_policy='static' limit 1");
            //Get the latest static value from the DB
            // Extract the time of the first entry
            epoch = (Long) tsdbData.getPoints().get(0).get(0);
            // Use the extracted epoch time to get all the data entry
            tsdbData = dbClient.getData("SELECT time,rate FROM rate WHERE rate_policy='static' AND time > "+epoch+"ms");
            // Construct the response
            return buildStaticRateResponse(tsdbData);
        }else{
            // Construct the response
            return buildDynamicRateResponse();
        }
    }

    private Representation buildStaticRateResponse(TSDBData tsdbData) {
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
        JsonRepresentation jsoneResp = new JsonRepresentation(jsonStr);
        return jsoneResp;
    }

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
        JsonRepresentation jsoneResp = new JsonRepresentation(jsonStr);
        return jsoneResp;
    }
}