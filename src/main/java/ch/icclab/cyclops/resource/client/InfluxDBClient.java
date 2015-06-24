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

import ch.icclab.cyclops.model.TSDBData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import ch.icclab.cyclops.util.Load;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 15-Oct-14
 * Description: Client class for InfluxDB
 *
 */
public class InfluxDBClient extends ClientResource {

    private String url;
    private String dbUsername;
    private String dbPassword;

    public InfluxDBClient(){
        this.url = Load.configuration.get("InfluxDbUrl");
        this.dbUsername = Load.configuration.get("InfluxDBUsername");
        this.dbPassword = Load.configuration.get("InfluxDBPassword");
    }

    /**
     * Saves the data into InfluxDB via HTTP
     *
     * Pseudo Code
     * 1. Load the login credentials from the configuration object
     * 2. Create a client instance and set the HTTP protocol, url and auth details
     * 3. Send the data
     *
     * @param data
     * @return boolean
     */
    public boolean saveData(String data){
        System.out.println("Entered the InfluxDBClient");

        data = "["+data+"]";
        Representation output;
        System.out.println(data);

        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC,dbUsername,dbPassword);

        cr.post(data);
        output = cr.getResponseEntity();

        System.out.println(output);
        System.out.println("Exit InfluxDBClient");

        return true;
    }

    /**
     * Fetches the data from InfluxDB via HTTP
     *
     * Pseudo Code
     * 1. Load the login credentials from the configuration object
     * 2. Create a client instance and set the HTTP protocol, url and auth details
     * 3. Query the db through its API
     * 4. Convert the json respnse into a TSDB java object
     *
     * @param query A query string
     * @return TSDBData
     */
    public TSDBData getData(String query){
        JSONArray resultArray;
        JSONObject resultObj;
        TSDBData dataObj = null;
        Representation output;
        ObjectMapper mapper = new ObjectMapper();

        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);

        cr.addQueryParameter("q",query);
        cr.addQueryParameter("u",dbUsername);
        cr.addQueryParameter("p",dbPassword);
        cr.get(MediaType.APPLICATION_JSON);
        output = cr.getResponseEntity();

        try {
            resultArray = new JSONArray(output.getText());
            resultObj = new JSONObject();
            resultObj = (JSONObject) resultArray.get(0);
            dataObj = mapper.readValue(resultObj.toString(),TSDBData.class);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataObj;
    }

}
