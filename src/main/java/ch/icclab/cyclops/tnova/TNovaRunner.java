package ch.icclab.cyclops.tnova;

import ch.icclab.cyclops.model.*;
import ch.icclab.cyclops.resource.client.AccountingClient;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.influxdb.dto.BatchPoints;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Thread that will take care of TNova UDR Usage Pulling
 */
public class TNovaRunner implements Runnable{
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
    protected TNovaRunner() {
        dbClient = new InfluxDBClient();
        puller = new TNovaPuller();
        accounting = new AccountingClient();
    }

    /**
     * This method gets called from outside in order to get data from TNova UDR and store it into database
     * @return
     */
    private void updateRecords() {

        // retrieve usage response from UDR micro-service
        UDRResponse response = puller.retrieveUsageResponseFromUDR();

        if (response != null) {
            BatchPoints container = dbClient.giveMeEmptyContainer();

            // now iterate over the list
            for (UDRRecord record : response.getUdr_records()) {

                // who is the customer of this selection?
                String customer = record.getClientId();

                // iterate over all the instanced he has
                for (UDREntry udrRow: record.getListOfEntries()) {

                    // now ask for billing model from accounting module
                    //BillingModel billingModel = accounting.getBillingModel(customer, udrRow.getInstanceId(), udrRow.getProductType());
                    McnBillingModel mcnBillingModel = dbClient.getBillingModel(customer, udrRow.getProductType(), udrRow.getTime());
                    //TODO get biling model for mcn (not using accounting, from influxdb) dbclient.getBillingModel
                    // we have received correct data
                    if (mcnBillingModel != null) {

                        // create CDR entry based on UDR entry and Billing model
                        CDREntry cdr = new CDREntry(udrRow, mcnBillingModel, customer);

                        // add point to container
                        container.point(cdr.toDBPoint());
                    }
                }
            }

            // and now finally save the container to DB
            dbClient.saveContainerToDB(container);
        }
    }

    public void run() {
        updateRecords();
    }
}
