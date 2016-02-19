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

package ch.icclab.cyclops.usecases.tnova;

import ch.icclab.cyclops.model.*;
import ch.icclab.cyclops.usecases.tnova.client.AccountingClient;
import ch.icclab.cyclops.database.InfluxDBClient;
import ch.icclab.cyclops.schedule.runner.AbstractRunner;
import ch.icclab.cyclops.usecases.tnova.model.RevenueSharingEntry;
import ch.icclab.cyclops.usecases.tnova.model.TnovaCDREntry;
import ch.icclab.cyclops.usecases.tnova.model.TnovaBillingModel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.influxdb.dto.BatchPoints;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Thread that will take care of TNova UDR Usage Pulling
 */
public class TNovaRunner extends AbstractRunner {
    final static Logger logger = LogManager.getLogger(TNovaRunner.class.getName());

    // connection to Database
    private static InfluxDBClient dbClient;

    // tnova puller
    private static TNovaPuller puller;

    // accounting module connection
    AccountingClient accounting;

    /**
     * Simple constructor that will create required connections
     */
    public TNovaRunner() {
        dbClient = new InfluxDBClient();
        puller = new TNovaPuller();
        accounting = new AccountingClient();
    }

    /**
     * This method gets called from outside in order to get data from TNova UDR and store it into database
     *
     * @return
     */
    private void updateRecords() {
        // flag used to debug.
        boolean cdrFlag = false;

        // retrieve usage response from UDR micro-service
        UDRResponse response = puller.retrieveUsageResponseFromUDR();

        if (response != null) {
            BatchPoints container = dbClient.giveMeEmptyContainer();

            // now iterate over the list
            for (UDRRecord record : response.getUdr_records()) {

                // who is the customer of this selection?
                String customer = record.getClientId();

                // iterate over all the instanced he has
                for (UDREntry udrRow : record.getListOfEntries()) {

                    // now ask for billing model from accounting module
                    BillingModel billingModel = accounting.getBillingModel(customer, udrRow.getInstanceId(), udrRow.getProductType());
                    // we have received correct data
                    if (billingModel != null) {

                        // create CDR entry based on UDR entry and Billing model
                        logger.debug("Attempting to create a CDR for the customer: " + customer);
                        TnovaCDREntry cdr = new TnovaCDREntry(udrRow, (TnovaBillingModel) billingModel, customer);
                        cdrFlag = true;
                        logger.debug("CDR Created.");
                        try {
                            if (udrRow.getProductType().equalsIgnoreCase("ns") || udrRow.getProductType().equalsIgnoreCase("vnf")) {
                                logger.debug("Attempting to create Revenue Sharing report.");
                                String[] relatives = udrRow.getRelatives().split(", ");
                                BatchPoints revenueSharingContainer = dbClient.giveMeEmptyContainer();
//                                for (int i = 0; i < relatives.length; i++) {
//                                    RevenueSharingEntry revenueSharing = new RevenueSharingEntry(cdr, relatives[i], (TnovaBillingModel) billingModel);
//                                    revenueSharingContainer.point(revenueSharing.toDBPoint());
//                                }
                                RevenueSharingEntry revenueSharing = new RevenueSharingEntry(cdr, udrRow.getInstanceId(), (TnovaBillingModel) billingModel);
                                revenueSharingContainer.point(revenueSharing.toDBPoint());
                                dbClient.saveContainerToDB(revenueSharingContainer);
                            }
                        } catch (Exception e) {
                            logger.error("Couldn't create the Revenue sharing for the service: " + e.getMessage());
                        }

                        // add point to container
                        container.point(cdr.toDBPoint());
                    }
                }
            }
            logger.debug("Attempting to store CDRs in the database.");
            if (cdrFlag)
                // and now finally save the container to DB
                dbClient.saveContainerToDB(container);
            if (cdrFlag)
                logger.debug("CDRs Correctly stored.");
        }
    }

    @Override
    public void run() {
        updateRecords();
    }
}