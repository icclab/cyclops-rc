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

import ch.icclab.cyclops.model.RateResponse;
import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import util.Flag;
import util.Load;

import java.io.IOException;
import java.util.*;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class RateResource extends ServerResource {
    @Get
    public Representation getRate(){
        TSDBData tsdbData = null;
        Representation response;
        Long epoch;
        InfluxDBClient dbClient = new InfluxDBClient();
        HashMap rateArr = new HashMap();

        String resourceName = getQueryValue("resourcename");
        String fromDate = getQueryValue("from");
        String toDate = getQueryValue("to");

        tsdbData = dbClient.getData("SELECT time,rate FROM rate WHERE resource='"+resourceName+"' AND time > '"+fromDate+"' AND time < '"+toDate+"'");
        rateArr.put(resourceName, tsdbData.getPoints());
        response = constructGetRateResponse(rateArr, resourceName, fromDate, toDate);
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
     * @param resourceName UserID for which the usage details is to be returned.
     * @param fromDate DateTime from usage data needs to be calculated
     * @param toDate DateTime upto which the usage data needs to be calculated
     * @return responseJson The response object in the JSON format
     */
    public Representation constructGetRateResponse(HashMap rateArr, String resourceName, String fromDate, String toDate){

        String jsonStr;
        JsonRepresentation responseJson = null;

        RateResponse responseObj = new RateResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();

        time.put("from",fromDate);
        time.put("to",toDate);

        //Build the response POJO
        responseObj.setTime(time);
        responseObj.setRate(rateArr);

        //Convert the POJO to a JSON string
        try {
            jsonStr = mapper.writeValueAsString(responseObj);
            responseJson = new JsonRepresentation(jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return responseJson;
    }

    @Post("json:json")
    public Representation setRate(Representation entity){
        JsonRepresentation request;
        JSONObject jsonObj;
        String ratingPolicy;

        // Get the JSON object from the incoming request
        try {
            request = new JsonRepresentation(entity);
            jsonObj = request.getJsonObject();
            ratingPolicy = (String) jsonObj.get("rate_policy");
            // Set the Metering Type flag according to the rating policy
            // For static rate, save the static rate of the resources
            if(ratingPolicy.equalsIgnoreCase("static")){
                Flag.setMeteringType("static");
                saveStaticRate(jsonObj, ratingPolicy);
            }else{
                Flag.setMeteringType("dynamic");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected Double getResourceRate(String resourceName, String from, String to){
        String query;
        TSDBData tsdbData;
        Double rate;
        InfluxDBClient dbClient = new InfluxDBClient();

        query = "SELECT mean(rate) FROM rate WHERE resource = '"+resourceName+"' AND time > '"+from+"' AND time < '"+to+"' ";
        tsdbData = dbClient.getData(query);
        rate = Double.parseDouble("" + tsdbData.getPoints().get(0).get(1));
        return rate ;
    }

    private boolean saveStaticRate(JSONObject jsonObj, String ratingPolicy) {
        boolean result = false;
        TSDBData rateData = new TSDBData();
        ArrayList<String> strArr = new ArrayList<String>();
        ArrayList<Object> objArrNode;
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        Load load = new Load();
        HashMap staticRate = new HashMap();
        String jsonData = null;
        JSONObject rateJsonObj;
        Iterator iterator;
        String key;

        // Reset the hashmap containing the static rate
        load.setStaticRate(staticRate);
        // Set the flag to "Static"
        Flag.setMeteringType("static");
        // Create the Array to hold the names of the columns
        strArr.add("resource");
        strArr.add("rate");
        strArr.add("rate_policy");
        try {
            rateJsonObj = (JSONObject) jsonObj.get("rate");
            iterator = rateJsonObj.keys();
            while(iterator.hasNext()){
                key = (String) iterator.next();
                objArrNode = new ArrayList<Object>();
                objArrNode.add(key);
                objArrNode.add(rateJsonObj.get(key));
                objArrNode.add(ratingPolicy);
                objArr.add(objArrNode);
                // Load the hashmap with the updates static rate
                staticRate.put(key,rateJsonObj.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Update the static hashmap containing the static rates
        load.setStaticRate(staticRate);
        // Set the data object to save into the DB
        rateData.setName("rate");
        rateData.setColumns(strArr);
        rateData.setPoints(objArr);
        try {
            jsonData = mapper.writeValueAsString(rateData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println(jsonData);
        result = dbClient.saveData(jsonData);
        return result;
    }

}
