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

import ch.icclab.cyclops.model.ChargeResponse;
import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.database.InfluxDBClient;
import ch.icclab.cyclops.util.APICallCounter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class ChargeResource extends ServerResource {

    // who am I?
    private String endpoint = "/charge";

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * Queries the database to get the charge data records for a given time period
     *
     * Pseudo Code
     * 1. Get the userid , from and to details from the API query parameters
     * 2. Query the database to get the cdr
     * 3. Construct the response and return the json string
     *
     * @return Representation
     */
    @Get
    public Representation getChargeRecords(){

        counter.increment(endpoint);

        InfluxDBClient dbClient = new InfluxDBClient();
        HashMap cdrMap = new HashMap();
        TSDBData tsdbData;
        Representation response;

        String userid = getQueryValue("userid");
        String fromDate = normalizeDateAndTime(getQueryValue("from"));
        String toDate = normalizeDateAndTime(getQueryValue("to"));

        //TODO: remove hard coded query
        tsdbData = dbClient.getData("SELECT * FROM mcn_cdr WHERE userId='"+userid+"' AND time > '"+fromDate+"' AND time < '"+toDate+"'");
        cdrMap.put("columns", tsdbData.getColumns());
        cdrMap.put("points", tsdbData.getPoints());
        response = constructResponse(cdrMap,userid,fromDate,toDate);
        return response;
    }

    /**
     *  Construct the JSON response consisting of the meter and the usage values
     *
     *  Pseudo Code
     *  1. Create the HasMap consisting of time range
     *  2. Create the response POJO
     *  3. Convert the POJO to JSON
     *  4. Return the JSON string
     *
     * @param rateArr An arraylist consisting of metername and corresponding usage
     * @param fromDate DateTime from usage data needs to be calculated
     * @param toDate DateTime upto which the usage data needs to be calculated
     * @return responseJson The response object in the JSON format
     */
    public Representation constructResponse(HashMap rateArr, String userid, String fromDate, String toDate){

        String jsonStr;
        JsonRepresentation responseJson = null;

        ChargeResponse responseObj = new ChargeResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();

        time.put("from",fromDate);
        time.put("to",toDate);

        //Build the response POJO
        responseObj.setUserid(userid);
        responseObj.setTime(time);
        responseObj.setCharge(rateArr);

        //Convert the POJO to a JSON string
        try {
            jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return responseJson;
    }

    /**
     * Remove ' character and replace T with a space
     * @param time
     * @return
     */
    private String normalizeDateAndTime(String time) {
        String first = time.replace("'", "");
        return first.replace("T", " ");
    }
}
