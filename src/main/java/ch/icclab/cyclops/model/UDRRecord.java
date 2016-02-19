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

package ch.icclab.cyclops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds UDR Record object (parsed from UDRResponse)
 */
public class UDRRecord {

    private String name;

    private List<String> columns;

    private List<List<String>> points;

    private Tag tags;

    public class Tag {
        private String clientId;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }

    /**
     * Parse out details from columns and based on header create objects
     * @return UDREntry object
     */
    public List<UDREntry> getListOfEntries () {
        List<UDREntry> list = new ArrayList<UDREntry>();

        // get indexes
        Integer time = columns.indexOf("time");
        Integer instanceId = columns.indexOf("instanceId");
        Integer relatives = columns.indexOf("relatives");
        Integer providerId = columns.indexOf("providerId");
        Integer type = columns.indexOf("productType");
        //Integer flagSetupCost = columns.indexOf("flagSetupCost");
        Integer to = columns.indexOf("to");
        Integer usage = columns.indexOf("usage");

        // iterate over all rows and fill the objects
        for (List<String> row : points) {
            UDREntry entry = new UDREntry();

            // fill everything
            entry.setTime(row.get(time));
            entry.setInstanceId(row.get(instanceId));
            entry.setProviderId(row.get(providerId));
            entry.setProductType(row.get(type));
            entry.setRelatives(row.get(relatives));
            //entry.setFlagSetupCost(row.get(flagSetupCost));
            entry.setTo(row.get(to));
            entry.setUsage(row.get(usage));

            // now add it to the list
            list.add(entry);
        }

        return list;
    }

    /**
     * Will return clientId of the records
     * @return
     */
    public String getClientId() {
        return tags.getClientId();
    }

    //============ Setters and Getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getPoints() {
        return points;
    }

    public void setPoints(List<List<String>> points) {
        this.points = points;
    }

    public Tag getTags() {
        return tags;
    }

    public void setTags(Tag tags) {
        this.tags = tags;
    }
}
