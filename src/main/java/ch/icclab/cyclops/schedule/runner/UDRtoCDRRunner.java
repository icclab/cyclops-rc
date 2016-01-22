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

package ch.icclab.cyclops.schedule.runner;

import ch.icclab.cyclops.services.iaas.cloudstack.Puller;
import ch.icclab.cyclops.services.iaas.cloudstack.model.UDRRecord;
import ch.icclab.cyclops.database.InfluxDBClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.influxdb.dto.BatchPoints;

import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 19-Nov-15
 * Description: Runner that will query UDR and create CDRs
 */
public class UDRtoCDRRunner extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(UDRtoCDRRunner.class.getName());

    private InfluxDBClient dbClient;

    public UDRtoCDRRunner() {
        dbClient = new InfluxDBClient();
    }

    @Override
    public void run() {
        logger.trace("Starting runner - will query UDR and create CDRs");

        updateRecords();
    }

    /**
     * Query UDR and create CDRs
     */
    private void updateRecords() {
        BatchPoints container = dbClient.giveMeEmptyContainer();

        Puller puller = new Puller();

        // get list of active meters
        List<String> meters = puller.getMeterList();

        // cycle over them
        for (String meter: meters) {

            // ask for UDR records of that particular meter
            UDRRecord records = puller.getUDRRecords(meter); //TODO only do it for recent data

            // now create CDRs based on UDRs
            records.populateContainerWithCDRs(container);
        }

        // save container to DB (empty container won't be saved)
        dbClient.saveContainerToDB(container);
    }
}
