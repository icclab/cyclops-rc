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
package ch.icclab.cyclops.usecases.openstack;

import ch.icclab.cyclops.database.InfluxDBClient;
import ch.icclab.cyclops.model.UDREntry;
import ch.icclab.cyclops.schedule.runner.AbstractRunner;
import ch.icclab.cyclops.usecases.openstack.model.OpenstackCollectorBillingModel;
import ch.icclab.cyclops.usecases.openstack.model.OpenstackCollectorCDREntry;
import ch.icclab.cyclops.usecases.openstack.model.OpenstackCollectorUDRRecord;
import ch.icclab.cyclops.usecases.openstack.model.OpenstackCollectorUDRResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.influxdb.dto.BatchPoints;

/**
 * Created by lexxito on 09.04.16.
 */
public class OpesntackCollectorRunner extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(OpesntackCollectorRunner.class.getName());

    // connection to Database
    private static InfluxDBClient dbClient;

    // tnova puller
    private static OpenstackCollectorPuller puller;

    // accounting module connection

    /**
     * Simple constructor that will create required connections
     */
    public OpesntackCollectorRunner() {
        dbClient = new InfluxDBClient();
        puller = new OpenstackCollectorPuller();
    }

    /**
     * This method gets called from outside in order to get data from TNova UDR and store it into database
     * @return
     */
    private void updateRecords() {

        // retrieve usage response from UDR micro-service
        OpenstackCollectorUDRResponse response = puller.retrieveUsageResponseFromUDR();

        if (response != null) {
            BatchPoints container = dbClient.giveMeEmptyContainer();

            // now iterate over the list
            for (OpenstackCollectorUDRRecord record : response.getUdr_records()) {

                // who is the customer of this selection?
                String customer = record.getClientId();

                // iterate over all the instanced he has
                for (UDREntry udrRow: record.getListOfEntries()) {

                    // now ask for billing model from accounting module
                    OpenstackCollectorBillingModel openstackCollectorBillingModel = dbClient.getOpenstackBillingModel(customer, udrRow.getProductType(), udrRow.getTime());
                    // we have received correct data
                    if (openstackCollectorBillingModel != null) {

                        // create CDR entry based on UDR entry and Billing model
                        OpenstackCollectorCDREntry cdr = new OpenstackCollectorCDREntry(udrRow, openstackCollectorBillingModel, customer);

                        // add point to container
                        container.point(cdr.toDBPoint());
                    }
                }
            }

            // and now finally save the container to DB
            dbClient.saveContainerToDB(container);
        }
    }

    @Override
    public void run() {
        updateRecords();
    }
}