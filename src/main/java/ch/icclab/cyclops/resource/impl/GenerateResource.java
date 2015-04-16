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

import ch.icclab.cyclops.model.ResourceUsage;
import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import ch.icclab.cyclops.resource.client.UDRServiceClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import util.DateTimeUtil;
import util.Flag;
import util.Load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Author: Srikanta
 * Created on: 25-Mar-15
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class GenerateResource extends ServerResource {
    private String action;
    private ArrayList enabledResourceList = new ArrayList();

    @Override
    public void doInit() {
        action = (String) getRequestAttributes().get("action");
    }

    @Get
    public String serviceRequest() throws IOException, JSONException {
        boolean rateResult = false;
        boolean cdrResult = false;

        // Get the list of enabled resources
        enabledResourceList = getEnabledResources();
        // Service the request
        if (action.equalsIgnoreCase("rate")) {
            rateResult = generateRate();
        } else if (action.equalsIgnoreCase("cdr")) {
            cdrResult = generateCdr();
        }
        // Construct the response
        if (rateResult) {
            return "The rate generation was successful";
        } else if (cdrResult) {
            return "The cdr generation was successful";
        } else {
            return "Operation Failed";
        }
    }

    private ArrayList getEnabledResources() {
        ArrayList enabledResourceList = new ArrayList();
        UDRServiceClient udrServiceClient = new UDRServiceClient();
        String meterData;
        JsonRepresentation responseJson;
        int meterNameIndex = 0;
        int meterStatusIndex = 0;
        JSONArray columnArr, pointsArr;
        // Get the active meters from the meter API from UDR Service
        try {
            meterData = udrServiceClient.getActiveResources();
            responseJson = new JsonRepresentation(meterData);
            JSONObject jsonObj = responseJson.getJsonObject();
            columnArr = jsonObj.getJSONArray("columns");

            for (int i = 0; i < columnArr.length(); i++) {
                if ("metername".equals(columnArr.get(i))) {
                    meterNameIndex = i;
                }
                if ("status".equals(columnArr.get(i))) {
                    meterStatusIndex = i;
                }
            }
            pointsArr = jsonObj.getJSONArray("points");
            Object meterArr = pointsArr.get(0);

            for (int j = 1; j < pointsArr.length(); j++) {
                JSONArray arr = (JSONArray) pointsArr.get(j);
                if (Integer.parseInt(arr.get(meterStatusIndex).toString()) == 1) {
                    enabledResourceList.add(arr.get(meterNameIndex));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return enabledResourceList;
    }

    private boolean generateRate(){
        boolean result = false;

        if(Flag.getMeteringType().equalsIgnoreCase("static")){
            result = saveStaticRate();
        }else{
            result = saveDynamicRate();
        }
        return result;
    }

    private boolean saveStaticRate() {
        ArrayList<String> strArr = new ArrayList<String>();
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        TSDBData rateData = new TSDBData();
        ObjectMapper mapper = new ObjectMapper();
        InfluxDBClient dbClient = new InfluxDBClient();
        ArrayList<Object> objArrNode;
        String jsonData = null;
        boolean result = false;
        Iterator<Map.Entry<String, Object>> entries;
        Object key;

        entries = Load.getStaticRate().entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<String, Object> entry = entries.next();
            key = entry.getKey();
            objArrNode = new ArrayList<Object>();
            objArrNode.add(key);
            objArrNode.add(entry.getValue());
            objArrNode.add("static");
            objArr.add(objArrNode);
        }

        // Set the columns
        strArr.add("resource");
        strArr.add("rate");
        strArr.add("rate_policy");
        // Set the data object to save into the DB
        rateData.setName("rate");
        rateData.setColumns(strArr);
        rateData.setPoints(objArr);
        try {
            jsonData = mapper.writeValueAsString(rateData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        result = dbClient.saveData(jsonData);
        return result;
    }

    private boolean saveDynamicRate(){
        double rate;
        Random rateGenerator = new Random();
        boolean result = false;
        TSDBData rateData = new TSDBData();
        ArrayList<String> strArr = null;
        ArrayList<Object> objArrNode;
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;

        // Iterate through the list and generate the rate for each enabled meters
        for(int k=0;k<enabledResourceList.size();k++){
            rate = rateGenerator.nextInt((3 - 1) + 1) + 1;
            strArr = new ArrayList<String>();
            strArr.add("resource");
            strArr.add("rate");
            strArr.add("rate_policy");
            objArrNode = new ArrayList<Object>();
            objArrNode.add(enabledResourceList.get(k));
            objArrNode.add(rate);
            objArrNode.add(Flag.getMeteringType());
            objArr.add(objArrNode);
        }

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

    private boolean generateCdr() throws IOException, JSONException {
        Object usage;
        double charge;
        String from, to;
        String[] time;
        int indexUserId, indexUsage;
        ArrayList usageListArr, usageArr;
        ArrayList columnArr;
        UDRServiceClient udrClient = new UDRServiceClient();
        RateResource rateResource = new RateResource();
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        Double rate;
        boolean result;
        String userid;
        ResourceUsage resourceUsageStr;
        ArrayList<Object> objArrNode;

        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        time = dateTimeUtil.getRange();

        from = time[1];
        to = time[0];

        for(int i=0; i<enabledResourceList.size(); i++){
            rate = rateResource.getResourceRate(enabledResourceList.get(i).toString(), from, to);
            resourceUsageStr = udrClient.getResourceUsageData(enabledResourceList.get(i).toString(),from,to);
            columnArr = resourceUsageStr.getColumn();
            usageListArr = resourceUsageStr.getUsage();
            indexUserId = columnArr.indexOf("userid");
            indexUsage = columnArr.indexOf("mean");
            // The below if condition differentiates between the gauge and cumulative meters of openstack
            if(indexUsage < 0){
                indexUsage = columnArr.indexOf("sum");
            }
            // Iterate through the usage arraylist to extract the userid and usage.
            // Multiple the usage with the rate of the resource and save it into an arraylist
            for(int j=0; j<usageListArr.size(); j++){
                usageArr = (ArrayList) usageListArr.get(j);
                for(int k=0; k<usageArr.size(); k++){
                    objArrNode = new ArrayList<Object>();
                    userid = (String) usageArr.get(indexUserId);
                    usage = usageArr.get(indexUsage);
                    // Calculate the charge for a resource per user
                    charge = (Double.parseDouble(usage.toString())*rate)/100000;           // TODO: Remove the hard coded divisor
                    objArrNode.add(enabledResourceList.get(i).toString());
                    objArrNode.add(userid);
                    objArrNode.add(usage);
                    objArrNode.add(charge);
                    objArr.add(objArrNode);
                }
            }
        }
        // Save the charge array into the database
        result = savePrice(objArr);
        return result;
    }

    private double getUserUsage(String usageData) {
        String meterName;
        JSONArray usageValue, srcArray;
        JSONObject meterObj,respObj,usageObj;
        JsonRepresentation jsonObj;
        String usageStr;
        double usage = 0;

        try {
            jsonObj = new JsonRepresentation(usageData);
            respObj = jsonObj.getJsonObject();
            usageObj = respObj.getJSONObject("usage");
            srcArray = usageObj.getJSONArray("openstack");

            for(int i=0; i<srcArray.length(); i++){
                meterObj = (JSONObject) srcArray.get(i);
                meterName = (String) meterObj.get("name");
                if(meterName.equals("network.incoming.bytes")){
                    usageValue = meterObj.getJSONArray("points");
                    usageStr =  usageValue.getJSONArray(0).get(1).toString();
                    usage = Double.parseDouble(usageStr);
                }
                System.out.println("Usage for "+meterName+" is"+usage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return usage;
    }

    private boolean savePrice(ArrayList<ArrayList<Object>> objArr) {
        boolean result = false;
        TSDBData pricingData = new TSDBData();
        ArrayList<String> strArr = new ArrayList<String>();
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;

        strArr.add("resource");
        strArr.add("userid");
        strArr.add("usage");
        strArr.add("price");
        pricingData.setName("cdr");
        pricingData.setColumns(strArr);
        pricingData.setPoints(objArr);

        try {
            jsonData = mapper.writeValueAsString(pricingData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println(jsonData);
        result = dbClient.saveData(jsonData);
        return result;
    }
}
