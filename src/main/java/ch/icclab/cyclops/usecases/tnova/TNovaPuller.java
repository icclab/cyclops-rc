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

package ch.icclab.cyclops.usecases.tnova;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.UDRResponse;
import ch.icclab.cyclops.database.InfluxDBClient;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.io.IOException;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Pull data from UDR
 */
public class TNovaPuller {
    final static Logger logger = LogManager.getLogger(TNovaPuller.class.getName());

    // endpoint URL for UDR
    String udrEndpoint;

    // connection to DB for getting last date
    InfluxDBClient dbClient;

    /**
     * Constructor that will populate class variables
     */
    public TNovaPuller() {
        this.udrEndpoint = Loader.getSettings().getCyclopsSettings().getUDRServiceUrl();
        this.dbClient = new InfluxDBClient();
    }

    public UDRResponse retrieveUsageResponseFromUDR() {
        // construct usage retrieval url
        String url = constructUsageQuery(dbClient.getLastPull());

        try {
            // retrieve response
            String response = pullData(url);

            // if it's empty return null
            if (response.isEmpty()) {
                return null;
            } else {
                // return parsed object
                return parseResponse(response);
            }

        } catch (Exception e) {
            logger.error("Couldn't retrieve response from UDR: " + e.getMessage());
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Parse provided response and return UDRResponse POJO object
     * @param response
     * @return object or null
     */
    private UDRResponse parseResponse(String response) {
        Gson gson = new Gson();

        return gson.fromJson(response, UDRResponse.class);
    }

    /**
     * Pull data from provided URL
     * @param url
     * @return output string or empty string
     */
    private String pullData(String url) throws IOException {
        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        Request req = cr.getRequest();

        // now header
        Series<Header> headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Accept", "application/json");
        headerValue.add("Content-Type", "application/json");

        // fire it up
        cr.get(MediaType.APPLICATION_JSON);
        Representation output = cr.getResponseEntity();

        // return null or received text
        return (output == null) ? "" : output.getText();
    }

    /**
     * Create T-Nova Usage query - with date specified
     * @param fromDate
     * @return
     */
    private String constructUsageQuery(String fromDate) {
        if (fromDate.isEmpty()) {
            return udrEndpoint + "/tnova/usage";
        } else {
            return udrEndpoint + "/tnova/usage?from=" + fromDate;
        }
    }
}
