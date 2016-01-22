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

package ch.icclab.cyclops.load;

import ch.icclab.cyclops.load.model.CyclopsSettings;
import ch.icclab.cyclops.load.model.InfluxDBSettings;
import ch.icclab.cyclops.load.model.SchedulerSettings;
import ch.icclab.cyclops.load.model.TNovaSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * @author Manu
 *         Created on 17.11.15.
 */
public class Settings {
    final static Logger logger = LogManager.getLogger(Settings.class.getName());

    // different settings options
    protected InfluxDBSettings influxDBSettings;
    protected SchedulerSettings schedulerSettings;
    protected CyclopsSettings cyclopsSettings;
    protected TNovaSettings tNovaSettings;

    private String environment;
    Properties properties;

    public Settings(Properties properties) {
        // parse environment settings
        environment = properties.getProperty("Environment");

        this.properties = properties;
    }

    public String getEnvironment() {
        return environment;
    }

    private InfluxDBSettings loadInfluxDBSettings() {
        return new InfluxDBSettings(properties.getProperty("InfluxDBURL"),
                properties.getProperty("InfluxDBUsername"), properties.getProperty("InfluxDBPassword"),
                properties.getProperty("dbName"), properties.getProperty("eventsDbName"));
    }

    private CyclopsSettings loadCyclopsSettings() {
        return new CyclopsSettings(properties.getProperty("UDRServiceUrl"), properties.getProperty("RuleEngine"), properties.getProperty("RCServiceUrl"));
    }

    public SchedulerSettings loadSchedulerSettings(){
        return new SchedulerSettings(properties.getProperty("SchedulerFrequency"));
    }

    public TNovaSettings loadTnovaSettings(){
        return new TNovaSettings(properties.getProperty("AccountingServiceUrl"));
    }

    public SchedulerSettings getSchedulerSettings() {
        if (schedulerSettings == null) {
            try {
                schedulerSettings = loadSchedulerSettings();
            } catch (Exception e) {
                logger.error("Could not load Scheduler settings from configuration file: " + e.getMessage());
            }
        }
        return schedulerSettings;
    }

    public CyclopsSettings getCyclopsSettings() {
        if (cyclopsSettings == null) {
            try {
                cyclopsSettings = loadCyclopsSettings();
            } catch (Exception e) {
                logger.error("Could not load Cyclops settings from configuration file: " + e.getMessage());
            }
        }
        return cyclopsSettings;
    }

    public TNovaSettings getTNovaSettings() {
        if (tNovaSettings == null) {
            try {
                tNovaSettings = loadTnovaSettings();
            } catch (Exception e) {
                logger.error("Could not load InfluxDB settings from configuration file: " + e.getMessage());
            }
        }
        return tNovaSettings;
    }

    public InfluxDBSettings getInfluxDBSettings() {
        if (influxDBSettings == null) {
            try {
                influxDBSettings = loadInfluxDBSettings();
            } catch (Exception e) {
                logger.error("Could not load InfluxDB settings from configuration file: " + e.getMessage());
            }
        }

        return influxDBSettings;
    }
}
