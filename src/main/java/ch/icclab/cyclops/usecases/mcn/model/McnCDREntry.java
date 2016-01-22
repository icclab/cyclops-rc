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

package ch.icclab.cyclops.usecases.mcn.model;

import ch.icclab.cyclops.model.BillingModel;
import ch.icclab.cyclops.model.UDREntry;
import ch.icclab.cyclops.util.DateTimeUtil;
import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Holds CDR Entry object after calculation of all required fields
 */
public class McnCDREntry {
    private String name = "mcn_cdr";
    private String time;
    private String productId;
    private String resource; // basically instanceId
    private String userId;   // basically clientId
    private double price;
    private String priceUnit;
    private String period;
    private double periodCost;
    private double setupCost;
    private String usage;

    /**
     * Construct and populate TnovaCDREntry object
     * @param udr object
     * @param model for billing
     * @param clientId as a string
     */
    public McnCDREntry(UDREntry udr, BillingModel model, String clientId) {
        // basic header
        this.time = udr.getTime();
        //this.productId = udr.getProductId();
        this.resource = udr.getInstanceId();
        this.userId = clientId;

        // compute price and save usage
        this.price = model.computeCost(Double.parseDouble(udr.getUsage()));
        this.usage = udr.getUsage();
    }

    /**
     * Create an InfluxDB Point that can be saved into InfluxDB database
     * @return
     */
    public Point toDBPoint() {

        Map tags = getTags();
        removeNullValues(tags);

        Map fields = getFields();
        removeNullValues(fields);

        // now return constructed point
        return Point.measurement(name)
                .time(DateTimeUtil.getMillisForTime(time), TimeUnit.MILLISECONDS)
                .tag(tags)
                .fields(fields)
                .build();
    }

    /**
     * This method returns default tags
     * @return
     */
    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        // now add default tags
        map.put("productId", productId);
        map.put("resource", resource);
        map.put("userId", userId);

        return map;
    }

    /**
     * This method returns default fields
     * @return
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        // now add default fields
        map.put("price", price);
        map.put("priceUnit", priceUnit);
        map.put("period", period);
        map.put("periodCost", periodCost);
        map.put("setupCost", setupCost);
        map.put("usage", usage);

        return map;
    }

    /**
     * Make sure we are not having any null values
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }
}
