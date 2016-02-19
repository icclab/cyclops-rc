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

import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds JSON Response from UDR Micro-service (used for GSON mapping)
 */
public class UDRResponse {

    private List<UDRRecord> udr_records;

    public List<UDRRecord> getUdr_records() {
        return udr_records;
    }

    public void setUdr_records(List<UDRRecord> udr_records) {
        this.udr_records = udr_records;
    }
}
