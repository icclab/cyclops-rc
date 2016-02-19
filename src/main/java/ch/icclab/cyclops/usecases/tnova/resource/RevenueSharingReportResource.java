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

package ch.icclab.cyclops.usecases.tnova.resource;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.ChargeResponse;
import ch.icclab.cyclops.usecases.tnova.model.RevenueSharingFullReport;
import ch.icclab.cyclops.usecases.tnova.model.RevenueSharingList;
import ch.icclab.cyclops.usecases.tnova.model.RevenueSharingReport;
import ch.icclab.cyclops.util.APICallCounter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Manu
 *         Created on 04.12.15.
 */
public class RevenueSharingReportResource extends ServerResource {
    final static Logger logger = LogManager.getLogger(RevenueSharingReportResource.class.getName());

    // who am I?
    private String endpoint = "/revenue/report";

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * Queries the database to get the charge data records for a given time period
     * <p/>
     * Pseudo Code
     * 1. Get the userid , from and to details from the API query parameters
     * 2. Query the database to get the cdr
     * 3. Construct the response and return the json string
     *
     * @return Representation
     */
    @Get
    public String getChargeRecords() {

        counter.increment(endpoint);

        Representation response;
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr;
        HashMap<String, Double> sum = new HashMap<String, Double>();

        String serviceProvider = getQueryValue("spId");
        String VNFProvider = getQueryValue("vfpId");
        String fromDate = normalizeDateAndTime(getQueryValue("from"));
        String toDate = normalizeDateAndTime(getQueryValue("to"));

        String urlParams;
        if (serviceProvider != null)
            urlParams = "/revenue?vfpId=" + VNFProvider + "&spId=" + serviceProvider + "&from=" + fromDate + "&to=" + toDate;
        else
            urlParams = "/revenue?vfpId=" + VNFProvider + "&from=" + fromDate + "&to=" + toDate;

        ClientResource clientResource = new ClientResource(Loader.getSettings().getCyclopsSettings().getRcServiceUrl() + urlParams);
        response = clientResource.get();
        RevenueSharingList revenueSharingList = null;
        try {
            jsonStr = response.getText();
            Gson gson = new Gson();
            revenueSharingList = gson.fromJson(jsonStr, RevenueSharingList.class);
//            sum = revenueSharingList.getAggregation();
        } catch (IOException e) {
            logger.debug("Error while mapping the Revenue Sharing object: " + e.getMessage());
        }

        RevenueSharingFullReport report = new RevenueSharingFullReport();
        report.setFrom(fromDate);
        report.setTo(toDate);
        report.setVNFProvider(VNFProvider);
        report.setRevenues(revenueSharingList.getPoints(), revenueSharingList.getColumns());
//        report.setPrice(sum);
//        report.setTime(fromDate, toDate);
//        report.setVFProvider(VNFProvider);
//        report.setSProvider(serviceProvider);

        Gson gson = new Gson();
        String result = gson.toJson(report);
        return result;
    }

    /**
     * Construct the JSON response consisting of the meter and the usage values
     * <p/>
     * Pseudo Code
     * 1. Create the HasMap consisting of time range
     * 2. Create the response POJO
     * 3. Convert the POJO to JSON
     * 4. Return the JSON string
     *
     * @param rateArr  An arraylist consisting of metername and corresponding usage
     * @param fromDate DateTime from usage data needs to be calculated
     * @param toDate   DateTime upto which the usage data needs to be calculated
     * @return responseJson The response object in the JSON format
     */
    public Representation constructResponse(HashMap rateArr, String userid, String fromDate, String toDate) {

        String jsonStr;
        JsonRepresentation responseJson = null;

        ChargeResponse responseObj = new ChargeResponse();
        HashMap time = new HashMap();
        ObjectMapper mapper = new ObjectMapper();

        time.put("from", fromDate);
        time.put("to", toDate);

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
     *
     * @param time
     * @return
     */
    private String normalizeDateAndTime(String time) {
        String first = time.replace("'", "");
        return first.replace("T", " ");
    }
}
