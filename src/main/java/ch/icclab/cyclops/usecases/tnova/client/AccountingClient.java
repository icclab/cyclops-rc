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

package ch.icclab.cyclops.usecases.tnova.client;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.model.BillingModel;
import ch.icclab.cyclops.usecases.tnova.model.TnovaBillingModel;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.io.IOException;

/**
 * Author: Skoviera
 * Created on: 02-Oct-15
 * Description: Client class for connecting to the T-Nova Accounting
 * <p/>
 * This class connects to T-Nova Accounting module (part of marketplace WP6) via its API.
 * It gets the VNF instances associated to a service instance, extracts the underlying
 * billing model and computes the charge per VNF.
 */

public class AccountingClient extends ClientResource {
    final static Logger logger = LogManager.getLogger(AccountingClient.class.getName());

    private String url;

    /**
     * Just a simple constructor
     */
    public AccountingClient() {
        this.url = Loader.getSettings().getTNovaSettings().getAccountingServiceUrl();
    }

    /**
     * Pull data from provided URL
     *
     * @param url
     * @return output string
     */
    private String pullData(String url) throws IOException {
        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        Request req = cr.getRequest();

        // now header
        Series<Header> headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Accept", "application/json");
        headerValue.add("Content-Type", "application/json");

        // fire it up
        cr.get(MediaType.APPLICATION_JSON);
        Representation output = cr.getResponseEntity();

        // and return response data
        return output.getText();
    }

    /**
     * Construct URL for Billing Model used with Accounting module
     *
     * @param clientId
     * @param instanceId
     * @param instanceType whether it is service or vnf
     * @return URL or null if instanceType is not supported
     */
    private String constructBillingModelURL(String clientId, String instanceId, String instanceType) {
        if (instanceType.equalsIgnoreCase("service")) {
            return url + "/service-billing-model/?format=json" + "&clientId=" + clientId + "&instanceId=" + instanceId;
        } else if (instanceType.equalsIgnoreCase("vnf")) {
            return url + "/vnf-billing-model/?format=json" + "&spId=" + clientId + "&instanceId=" + instanceId;
        } else {
            return null;
        }
    }

    /**
     * Parse Billing Model response
     *
     * @param response
     * @return
     */
    private TnovaBillingModel parseBillingModel(String response) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(response, TnovaBillingModel.class);
        }catch (Exception e){
            //For VNFs the accounting module returns an array instead of a jsonObject
            return gson.fromJson(response, TnovaBillingModel[].class)[0];
        }
    }

    /**
     * Retrieve Billing Model from Accounting module
     *
     * @param clientId
     * @param instanceId
     * @param instanceType whether it is service or vnf
     * @return BillingModel object or null
     */
    public BillingModel getBillingModel(String clientId, String instanceId, String instanceType) {
        logger.debug("Call to Accounting: Attempting to get the billing model for the client: " + clientId + " and instance: " + instanceId);
        TnovaBillingModel billingModel = null;

        // construct URL
        String completeUrl = constructBillingModelURL(clientId, instanceId, instanceType);

        // if we support this service type
        if (completeUrl != null) {
            // now pull data from the url
            try {
                String response = pullData(completeUrl);

                // and parse it finally
                billingModel = parseBillingModel(response);

            } catch (Exception e) {
                logger.error("Couldn't pull data from Accounting module for " + completeUrl);
            }
        } else {
            logger.error("InstanceType: " + instanceType + " not supported");
        }
        logger.debug("Obtained billing model: "+ billingModel.getBillingModel());
        return billingModel;
    }
}

