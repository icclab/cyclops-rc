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

import ch.icclab.cyclops.database.DatabaseHelper;
import ch.icclab.cyclops.database.InfluxDBClient;
import ch.icclab.cyclops.model.BillingModel;
import org.influxdb.dto.QueryResult;

/**
 * Author: Martin Skoviera
 * Created on: 19-Nov-15
 * Description: Billing model for CloudStack
 */
public class CloudStackBillingModel extends BillingModel{

    private String metername;
    private String from;
    private InfluxDBClient dbClient;

    public CloudStackBillingModel(String metername, String from) {
        this.metername = metername;
        this.from = from;
        dbClient = new InfluxDBClient();
    }

    @Override
    public Double computeCost(double time) {

        // first ask for value for the exact time
        String query = DatabaseHelper.getRateQuery(metername, from);
        QueryResult result = dbClient.runQuery(query);

        try {
            // access value directly
            Double value = Double.parseDouble(result.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString());
            return time*value;
        } catch (Exception ignored) {

            // try again with the most recent rate
            query = DatabaseHelper.getRateQuery(metername);
            result = dbClient.runQuery(query);

            try {

                // try to parse it and return its multiplication
                Double value = Double.parseDouble(result.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString());
                return time*value;

            } catch (Exception ign) {
                // this means we didn't have rate for particular meter
            }
        }
        // we couldn't get
        return null;
    }
}
