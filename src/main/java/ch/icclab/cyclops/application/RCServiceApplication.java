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
import org.restlet.Application;
import org.restlet.Restlet;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description: The routing class which routes the incoming API request to its corresponding resource class.
 * It also loads the config file at the start of the service
 *
 */
public class RCServiceApplication extends Application {

    @Override
    public Restlet createInboundRoot(){
        return AbstractApplicationFactory.getApplication(getContext());
    }

}
