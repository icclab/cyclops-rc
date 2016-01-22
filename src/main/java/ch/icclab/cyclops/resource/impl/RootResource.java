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

package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.util.APICallCounter;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Author: Srikanta
 * Created on: 16-Feb-15
 * Description: Root class for /rc API endpoint
 */
public class RootResource extends ServerResource {

    // who am I?
    private String endpoint = "/";

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    /**
     * Returns a string that identifies the application
     *
     * @return String
     */
    @Get
    public String rootMsg(){
        counter.increment(endpoint);

        return "CYCLOPS RC Service v0.2.1";
    }
}
