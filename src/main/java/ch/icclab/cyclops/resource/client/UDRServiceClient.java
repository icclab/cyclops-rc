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
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import util.Load;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description:
 * <p/>
 * Change Log
 * Name        Date     Comments
 */
public class UDRServiceClient extends ClientResource {

    private String url = Load.configuration.get("UDRServiceUrl");

    public ResourceUsage getResourceUsageData(String resourceName, String from, String to) throws IOException {
        ResourceUsage tsdbData = null;
        JSONObject resultArray;
        JSONObject resultObj;
        ObjectMapper mapper = new ObjectMapper();

        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"/usage/resources/"+resourceName);
        resource.getReference().addQueryParameter("from",from.toString());
        resource.getReference().addQueryParameter("to",to.toString());
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        try {
            resultArray = new JSONObject(output.getText());
            tsdbData = mapper.readValue(resultArray.toString(),ResourceUsage.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tsdbData;
    }


    public String getActiveResources() throws IOException{
        Request req;

        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"/meters");
        req = resource.getRequest();
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        String result = output.getText();
        return result;
    }
}
