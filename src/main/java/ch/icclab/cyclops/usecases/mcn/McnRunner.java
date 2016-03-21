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

package ch.icclab.cyclops.usecases.mcn;

import ch.icclab.cyclops.database.InfluxDBClient;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.*;
import ch.icclab.cyclops.schedule.runner.AbstractRunner;
import ch.icclab.cyclops.usecases.mcn.model.McnBillingModel;
import ch.icclab.cyclops.usecases.mcn.model.McnCDREntry;
import ch.icclab.cyclops.usecases.mcn.model.McnUDRRecord;
import ch.icclab.cyclops.usecases.mcn.model.McnUDRResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.influxdb.dto.BatchPoints;
import org.restlet.engine.Edition;
import org.restlet.resource.ClientResource;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Thread that will take care of TNova UDR Usage Pulling
 */
public class McnRunner extends AbstractRunner{
    final static Logger logger = LogManager.getLogger(McnRunner.class.getName());

    // connection to Database
    private static InfluxDBClient dbClient;

    // tnova puller
    private static McnPuller puller;

    // accounting module connection

    /**
     * Simple constructor that will create required connections
     */
    public McnRunner() {
        dbClient = new InfluxDBClient();
        puller = new McnPuller();
    }

    /**
     * This method gets called from outside in order to get data from TNova UDR and store it into database
     * @return
     */
    private void updateRecords() {

        ClientResource clientResource = new ClientResource(Loader.getSettings().getCyclopsSettings().getRcServiceUrl()+"/generate/rate");
        clientResource.get();
        clientResource = new ClientResource(Loader.getSettings().getCyclopsSettings().getRcServiceUrl()+"/generate/cdr");
        clientResource.get();
        // retrieve usage response from UDR micro-service
        McnUDRResponse response = puller.retrieveUsageResponseFromUDR();

        if (response != null) {
            BatchPoints container = dbClient.giveMeEmptyContainer();

            // now iterate over the list
            for (McnUDRRecord record : response.getUdr_records()) {

                // who is the customer of this selection?
                String customer = record.getClientId();

                // iterate over all the instanced he has
                for (UDREntry udrRow: record.getListOfEntries()) {

                    // now ask for billing model from accounting module
                    McnBillingModel mcnBillingModel = dbClient.getBillingModel(customer, udrRow.getProductType(), udrRow.getTime());
                    //TODO get biling model for mcn (not using accounting, from influxdb) dbclient.getBillingModel
                    // we have received correct data
                    if (mcnBillingModel != null) {

                        // create CDR entry based on UDR entry and Billing model
                        McnCDREntry cdr = new McnCDREntry(udrRow, mcnBillingModel, customer);

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
