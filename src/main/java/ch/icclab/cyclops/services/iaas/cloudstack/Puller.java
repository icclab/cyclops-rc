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

package ch.icclab.cyclops.services.iaas.cloudstack;

import ch.icclab.cyclops.services.iaas.cloudstack.model.UDRRecord;
import ch.icclab.cyclops.util.URLHelper;
import ch.icclab.cyclops.model.MeterList;
import com.google.gson.Gson;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 19-Nov-15
 * Description: Puller for getting data out of UDR microservice
 */
public class Puller {

    public List<String> getMeterList() {
        List<String> meterList = new ArrayList<String>();

        // construct url
        String url = URLHelper.getUDRMeterListURL();

        // get data
        String result = pullData(url);

        // parse list
        MeterList list = parseMeterList(result);

        // add only ones that are enabled
        meterList.addAll(list.getEnabledMeters());

        return meterList;
    }

    public UDRRecord getUDRRecords(String meterName) {
        String url = URLHelper.getUDRUsageResourceURL(meterName);

        // pull data from UDR
        String result = pullData(url);

        // parse it
        UDRRecord record = parseUDRRecord(result);

        return record;
    }

    /**
     * Pull data from provided URL
     * @param url
     * @return output string or empty string
     */
    private String pullData(String url){
        try {
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
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Parse JSON to MeterList
     * @param response string
     * @return MeterList object
     */
    private MeterList parseMeterList(String response) {
        return new Gson().fromJson(response, MeterList.class);
    }

    private UDRRecord parseUDRRecord(String response) {
        return new Gson().fromJson(response, UDRRecord.class);
    }
}
