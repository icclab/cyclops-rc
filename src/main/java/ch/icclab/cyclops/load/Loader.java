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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;

import javax.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Martin
 * Created by root on 17.11.15.
 */
public class Loader {
    final static Logger logger = LogManager.getLogger(Loader.class.getName());

    // singleton pattern
    private static Loader singleton;

    // loaded settings and environment
    private Settings settings;

    /**
     * Constructor has to be private, as we are using singleton
     */
    private Loader(Context context) {
        // only if object is created by createInstance (which gives it context)
        if (context != null) {
            // start with loading config file
            Properties properties = loadAndParseConfigurationFile(context);

            // now parse it and save it
            settings = new Settings(properties);
        }
    }

    /**
     * Ask for environmental settings
     * @return string or null
     */
    public static String getEnvironment() {
        if (singleton.settings != null) {
            return singleton.settings.getEnvironment();
        } else {
            return null;
        }
    }

    /**
     * When creating instance, we need it to have context
     * @param c as context
     * @return instance pointer
     */
    public static Loader createInstance(Context c) {
        if (singleton == null) {
            singleton = new Loader(c);
        }

        return singleton;
    }

    /**
     * Access settings from Loader class
     * @return settings object or null
     */
    public static Settings getSettings() {
        if (singleton.settings != null) {
            return singleton.settings;
        } else {
            return null;
        }
    }

    /**
     * Load and parse configuration file
     * @return null or property object
     */
    private Properties loadAndParseConfigurationFile(Context context) {
        // get the path of the config file relative to the WAR
        ServletContext servlet = (ServletContext) context.getAttributes().get("org.restlet.ext.servlet.ServletContext");

        // store everything into property variable
        Properties prop = new Properties();

        try {
            // load file
            InputStream input = servlet.getResourceAsStream("/WEB-INF/configuration.txt");

            // now feed input file to property loader
            prop.load(input);

            return prop;

        } catch (FileNotFoundException e) {
            logger.error("Configuration file doesn't exist or cannot be loaded: " + e.getMessage());
            return null;
        } catch (IOException e) {
            logger.error("Couldn't load configuration file: " + e.getMessage());
            return null;
        }
    }
}
