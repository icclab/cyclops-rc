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

package ch.icclab.cyclops.util;

import com.google.gson.Gson;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.HashMap;

/**
 * Author: Martin Skoviera
 * Created on: 04-Nov-15
 * Description: This class handles the internal APICallCounter
 */
public class APICallEndpoint extends ServerResource {
    final static Logger logger = LogManager.getLogger(APICallEndpoint.class.getName());

    // used as counter
    private APICallCounter counter = APICallCounter.getInstance();

    // who am I?
    private String endpoint = "/status";

    /**
     * This method will return JSON stats for APICallCounter object
     * @return JSON
     */
    @Get
    public String processCommand() {

        // we should also log this entry
        counter.increment(endpoint);

        // first get running statistics
        HashMap<String, Integer> stats = counter.getRunningStats();

        // now transform it to JSON
        Gson gson = new Gson();
        String json = gson.toJson(stats);

        // and return
        return json;
    }
}
