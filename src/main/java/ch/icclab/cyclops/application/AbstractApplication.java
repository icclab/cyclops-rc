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

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.Settings;
import ch.icclab.cyclops.database.InfluxDBClient;
import ch.icclab.cyclops.resource.impl.RootResource;
import ch.icclab.cyclops.util.APICallCounter;
import ch.icclab.cyclops.util.APICallEndpoint;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * @author Manu
 * Created by root on 16.11.15.
 */
public abstract class AbstractApplication extends Application{
    // start by providing context
    Context context = getContext();

    // router for registering api endpoints
    Router router = new Router(context);

    // get environment settings
    Settings settings = Loader.getSettings();

    InfluxDBClient dbClient = new InfluxDBClient();

    // create the api counter
    APICallCounter counter = APICallCounter.getInstance();

    public Restlet createInboundRoot(){

        router.attach("/", RootResource.class);
        counter.registerEndpoint("/");

        router.attach("/status", APICallEndpoint.class);
        counter.registerEndpoint("/status");

        createRoutes();

        initialiseDatabases();

        return router;
    }

    public abstract void createRoutes();

    public abstract void initialiseDatabases();
}
