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

package ch.icclab.cyclops.usecases.mcn.model;

import ch.icclab.cyclops.model.BillingModel;

/**
 * @author Manu
 * Created by root on 06.11.15.
 */
public class McnBillingModel extends BillingModel {
    //used for mapping
    private double price = 0.0;
    private String rate_policy;
    private String resource;
    private String date;

    @Override
    public Double computeCost(double time) {
        return this.price*time;
    }

    //getters and setters

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRate_policy() {
        return rate_policy;
    }

    public void setRate_policy(String rate_policy) {
        this.rate_policy = rate_policy;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
