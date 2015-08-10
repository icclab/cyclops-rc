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

/**
 * Author: Srikanta
 * Created on: 18-Mar-15
 * Description: A POJO class for storing the flags. Flags are used to distinguish between the rating policy used at any instant
 *
 */
public class Flag {
    private static String meteringType = "dynamic";

    public static String getMeteringType() {
        return meteringType;
    }

    public static void setMeteringType(String meteringType) {
        Flag.meteringType = meteringType;
    }
}
