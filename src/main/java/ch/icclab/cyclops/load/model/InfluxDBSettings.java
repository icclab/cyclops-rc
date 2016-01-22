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

package ch.icclab.cyclops.load.model;

/**
 * @author Manu
 *         Created on 17.11.15.
 */
public class InfluxDBSettings {
    private String url;
    private String user;
    private String password;
    private String dbName;
    private String eventsDbName;

    public InfluxDBSettings(String influxDBURL, String influxDBUser, String influxDBPassword, String dbName, String eventsDbName) {
        this.url = influxDBURL;
        this.user = influxDBUser;
        this.password = influxDBPassword;
        this.dbName = dbName;
        this.eventsDbName = eventsDbName;
    }

    public String getEventsDbName() {
        return eventsDbName;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDbName() {
        return dbName;
    }
}
