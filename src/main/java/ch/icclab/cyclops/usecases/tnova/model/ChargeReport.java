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

package ch.icclab.cyclops.usecases.tnova.model;

import java.util.HashMap;

/**
 * @author Manu
 *         Created on 08.12.15.
 */
public class ChargeReport {
    private Double price;
    private HashMap<String, String> time;
    private String userid;

    public void setTime(String from, String to) {
        this.time = new HashMap<String, String>();
        this.time.put("from", from);
        this.time.put("to", to);
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
