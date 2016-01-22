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

import java.util.ArrayList;

/**
 * @author Manu
 *         Created on 09.12.15.
 */
public class TnovaChargeResponse {
//    private String name = "charge";
    private String time;
    private String userId;
    private String resource;
    private String providerId;
    private String usage;
    private Double price;
    private Double setupCost;
    private Double periodCost;
    private String priceUnit;

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSetupCost() {
        return setupCost;
    }

    public void setSetupCost(Double setupCost) {
        this.setupCost = setupCost;
    }

    public Double getPeriodCost() {
        return periodCost;
    }

    public void setPeriodCost(Double periodCost) {
        this.periodCost = periodCost;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public void setFields(ArrayList<Object> value, ArrayList<String> columns) {
        this.setTime((String) value.get(columns.indexOf("time")));
        this.setUserId((String) value.get(columns.indexOf("userId")));
        this.setResource((String) value.get(columns.indexOf("resource")));
        this.setProviderId((String) value.get(columns.indexOf("providerId")));
        this.setUsage((String) value.get(columns.indexOf("usage")));
        this.setPrice(getDoubleValue(value.get(columns.indexOf("price"))));
        this.setSetupCost(getDoubleValue(value.get(columns.indexOf("setupCost"))));
        this.setPeriodCost(getDoubleValue(value.get(columns.indexOf("periodCost"))));
        this.setPriceUnit((String) value.get(columns.indexOf("priceUnit")));
    }

    private Double getDoubleValue(Object number){
        try{
            return (Double) number;
        }catch (Exception e){
            return Double.parseDouble(number.toString());
        }
    }
}
