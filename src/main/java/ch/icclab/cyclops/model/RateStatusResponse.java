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

import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 26-Mar-15
 * Description: The class is used for constructing the response for a rate policy status API request
 */
public class RateStatusResponse {
    private String rate_policy;
    private HashMap rate;

    public String getRate_policy() {
        return rate_policy;
    }

    public void setRate_policy(String rate_policy) {
        this.rate_policy = rate_policy;
    }

    public HashMap getRate() {
        return rate;
    }

    public void setRate(HashMap rate) {
        this.rate = rate;
    }
}
