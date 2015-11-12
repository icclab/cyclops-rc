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

package ch.icclab.cyclops.application;

import ch.icclab.cyclops.schedule.Endpoint;
import ch.icclab.cyclops.resource.impl.*;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.APICallEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import ch.icclab.cyclops.util.Load;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description: The routing class which routes the incoming API request to its corresponding resource class.
 * It also loads the config file at the start of the service
 *
 */
public class RCServiceApplication extends Application{
    final static Logger logger = LogManager.getLogger(RCServiceApplication.class.getName());

    /**
     * Loads the configuration file at the beginning of the application startup
     *
     * Pseudo Code
     * 1. Load the config file
     * 2. Route the incoming API request to its corresponding resource class
     *
     * @return Restlet
     */
    public Restlet createInboundRoot(){
        //Load the configuration files and flags
        loadConfiguration(getContext());
        // Router for the incoming the API request
        Router router = new Router();

        // get instance of the counter class
        APICallCounter counter = APICallCounter.getInstance();

        router.attach("/", RootResource.class);
        counter.registerEndpoint("/");

        router.attach("/rate", RateResource.class);
        counter.registerEndpoint("/rate");

        router.attach("/rate/status", RateStatusResource.class);
        counter.registerEndpoint("/rate/status");

        router.attach("/charge", ChargeResource.class);
        counter.registerEndpoint("/charge");

        router.attach("/generate/{action}", GenerateResource.class);
        counter.registerEndpoint("/generate");

        router.attach("/scheduler/{command}", Endpoint.class);
        counter.registerEndpoint("/scheduler");

        router.attach("/status", APICallEndpoint.class);
        counter.registerEndpoint("/status");

        // but also start scheduler immediately
        startInternalScheduler();

        return router;
    }

    /**
     * Simply start internal scheduler for Event -> UDR
     */
    private void startInternalScheduler() {
        Scheduler.getInstance().start();
    }

    /**
     * Loads the configuration file at the beginning of the application startup
     *
     * Pseudo Code
     * 1. Create the LoadConfiguration class
     * 2. Load the file if the the existing instance of the class is empty
     *
     * @param context
     * @return Void
     */
    private void loadConfiguration(Context context){
        logger.trace("BEGIN void loadConfiguration(Context context)");
        Load load = new Load();
        if(load.configuration == null){
            load.configFile(getContext());
            load.createDatabase();
        }
        logger.trace("END void loadConfiguration(Context context)");
    }

}
