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

import ch.icclab.cyclops.applicationFactory.AbstractApplicationFactory;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.resource.impl.ChargeResource;
import ch.icclab.cyclops.resource.impl.GenerateResource;
import ch.icclab.cyclops.resource.impl.RateResource;
import ch.icclab.cyclops.resource.impl.RateStatusResource;
import ch.icclab.cyclops.schedule.Endpoint;
import ch.icclab.cyclops.schedule.Scheduler;
import ch.icclab.cyclops.services.iaas.openstack.CDRGeneration;
import ch.icclab.cyclops.usecases.mcn.McnRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Application;
import org.restlet.Restlet;

import java.util.concurrent.TimeUnit;


public class OpenstackRCServiceApplication extends AbstractApplication{
    final static Logger logger = LogManager.getLogger(OpenstackRCServiceApplication.class.getName());

    @Override
    public void createRoutes() {
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

        startInternalScheduler();
    }

    private void startInternalScheduler() {
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.addRunner(new CDRGeneration(), 0, Long.valueOf(Loader.getSettings().getSchedulerSettings().getSchedulerFrequency()), TimeUnit.SECONDS);
        scheduler.start();
    }

    @Override
    public void initialiseDatabases() {
        dbClient.createDatabases(settings.getInfluxDBSettings().getDbName());
    }

}
