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

import ch.icclab.cyclops.resource.impl.*;
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
        logger.trace("BEGIN Restlet createInboundRoot()");
        //Load the configuration files and flags
        loadConfiguration(getContext());
        // Router for the incoming the API request
        Router router = new Router();
        router.attach("/", RootResource.class);
        router.attach("/rate", RateResource.class);
        router.attach("/rate/status", RateStatusResource.class);
        router.attach("/charge", ChargeResource.class);
        router.attach("/generate/{action}", GenerateResource.class);
        logger.trace("END Restlet createInboundRoot()");

        return router;
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
        }
        logger.trace("END void loadConfiguration(Context context)");
    }

}
