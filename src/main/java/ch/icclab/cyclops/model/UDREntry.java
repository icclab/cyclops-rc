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

package ch.icclab.cyclops.model;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds UDR Entry, as a row line from UDR Record
 */
public class UDREntry {
    private String time;
    private String instanceId;
    private String productId;
    private String productType;
    private String flagSetupCost;
    private String to;
    private String usage;
    private String providerId;
    private String relatives;

    /**
     * Will return boolean based on flagSetupCost
     * @return
     */

    public Boolean shouldAddSetupCost() {
        return Boolean.valueOf(flagSetupCost);
    }

    //=========== Setters and Getters

    public String getRelatives() {
        return relatives;
    }

    public void setRelatives(String relatives) {
        this.relatives = relatives;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String type) {
        this.productType = type;
    }

    public String getFlagSetupCost() {
        return flagSetupCost;
    }

    public void setFlagSetupCost(String flagSetupCost) {
        this.flagSetupCost = flagSetupCost;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }
}
