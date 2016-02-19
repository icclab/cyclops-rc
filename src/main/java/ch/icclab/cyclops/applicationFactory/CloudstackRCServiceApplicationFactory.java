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

package ch.icclab.cyclops.applicationFactory;

import ch.icclab.cyclops.application.AbstractApplication;
import ch.icclab.cyclops.application.CloudstackRCServiceApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Martin Skoviera
 * Date: 18/11/2015
 * Description: Factory for CloudStack
 */
public class CloudstackRCServiceApplicationFactory extends AbstractApplicationFactory {
    final static Logger logger = LogManager.getLogger(CloudstackRCServiceApplicationFactory.class.getName());

    @Override
    public AbstractApplication loadApplication() {
        logger.debug("Creating routes for a new CloudStack RCService.");
        return new CloudstackRCServiceApplication();
    }
}