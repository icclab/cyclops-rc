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

import org.joda.time.DateTime;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds Billing Model JSON Response from Accounting module (used for mapping and calculations)
 */
public abstract class BillingModel {

    /**
     * Compute cost for the billing model and provided time
     * @param time has to be of double data primitive
     */
    public abstract Double computeCost(double time);
}
