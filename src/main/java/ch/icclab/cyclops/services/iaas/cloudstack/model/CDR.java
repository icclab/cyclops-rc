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

package ch.icclab.cyclops.services.iaas.cloudstack.model;

import ch.icclab.cyclops.util.DateTimeUtil;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 19-Nov-15
 * Description: Create CDR entry from UDR for CloudStack
 */
public class CDR {
    private static String name = "cloudstack_cdr";
    private String time;
    private String resource;
    private String userId;
    private double price;
    private String usage;

    public CDR(String time, String resource, String userId, String usage, double price) {
        this.time = time;
        this.resource = resource;
        this.userId = userId;
        this.usage = usage;
        this.price = price;
    }

    public Point toDBPoint() {
        Map tags = getTags();
        Map fields = getFields();

        // now return constructed point
        return Point.measurement(name)
                .time(DateTimeUtil.getMillisForTime(time), TimeUnit.MILLISECONDS)
                .tag(tags)
                .fields(fields)
                .build();
    }

    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        // now add default tags
        map.put("resource", resource);
        map.put("userId", userId);

        return map;
    }

    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        // now add default fields
        map.put("price", price);
        map.put("usage", usage);

        return map;
    }

    public static String getCDRColumnName() {
        return name;
    }
}
