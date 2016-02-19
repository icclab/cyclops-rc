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

package ch.icclab.cyclops.services.iaas.cloudstack.model;

/**
 * Author: Martin Skoviera
 * Created on: 19-Nov-15
 * Description: POJO template for CloudStack UDR entry
 */
public class UDREntry {
    private String time;
    private String rawusage;
    private String projectid;
    private String account;
    private String metername;

    public void setTime(String time) {
        this.time = time;
    }
    public void setRawusage(String rawusage) {
        this.rawusage = rawusage;
    }
    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public void setMetername(String metername) {
        this.metername = metername;
    }

    /**
     * Ask for CDR based on UDR entry
     * @return CDR or null if we didn't a have rate for selected meter
     */
    public CDR getCDR() {
        String userid;

        // save user id as account or project id
        if (projectid != null && !projectid.isEmpty()) {
            userid = projectid;
        } else {
            userid = account;
        }

        // create new billing model
        CloudStackBillingModel model = new CloudStackBillingModel(metername, time);

        // compute price
        Double price = null;
        try {
            price = model.computeCost(Double.parseDouble(rawusage));
        } catch (Exception ignored) {
        }

        // return CDR only if we have correct price
        if (price != null) {
            return new CDR(time, metername, userid, rawusage, price);
        } else {
            return null;
        }
    }
}
