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

import org.influxdb.dto.BatchPoints;

import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 19-Nov-15
 * Description: POJO template for CloudStack UDR records
 */
public class UDRRecord {
    private String name;
    private List<String> columns;
    private List<List<String>> points;

    public void setName(String name) {
        this.name = name;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public void setPoints(List<List<String>> points) {
        this.points = points;
    }

    public void populateContainerWithCDRs(BatchPoints container) {
        // get indexes
        Integer timeIndex = columns.indexOf("time");
        Integer rawUsageIndex = columns.indexOf("rawusage");
        Integer projectidIndex = columns.indexOf("projectid");
        Integer accountIndex = columns.indexOf("account");

        // create UDREntries
        for (List<String> point : points) {
            UDREntry entry = new UDREntry();

            entry.setTime(point.get(timeIndex));
            entry.setRawusage(point.get(rawUsageIndex));
            entry.setProjectid(point.get(projectidIndex));
            entry.setAccount(point.get(accountIndex));
            entry.setMetername(name);

            // now save CDR to container
            CDR cdr = entry.getCDR();

            // only add if we have valid CDR
            if (cdr != null) {
                container.point(cdr.toDBPoint());
            }
        }
    }
}
