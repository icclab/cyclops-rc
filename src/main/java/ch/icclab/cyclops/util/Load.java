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

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Author: Srikanta
 * Created on: 25-Mar-15
 * Description: This class loads the configuration file and other static maps
 *
 */
public class Load {
    private InfluxDB influxDB;
    public static HashMap<String,String> configuration;
    private static HashMap staticRate;

    public static HashMap getStaticRate() {
        return staticRate;
    }

    public static void setStaticRate(HashMap staticRate) {
        Load.staticRate = staticRate;
    }

    public void configFile(Context context){
        configuration = new HashMap();
        String nextLine;
        ServletContext servlet = (ServletContext) context.getAttributes().get("org.restlet.ext.servlet.ServletContext");
        System.out.println("Reading the config file from " + servlet.getRealPath("/WEB-INF/configuration.txt"));
        // Get the path of the config file relative to the WAR
        String rootPath = servlet.getRealPath("/WEB-INF/configuration.txt");
        Path path = Paths.get(rootPath);
        File configFile = new File(path.toString());
        FileRepresentation file = new FileRepresentation(configFile, MediaType.TEXT_PLAIN);
        // Read the values from the config file
        try {
            BufferedReader reader = new BufferedReader(file.getReader());
            while((nextLine = reader.readLine()) != null ) {
                String[] str = nextLine.split("==");
                configuration.put(str[0],str[1]);
            }
        } catch (IOException e) {
            System.out.println("Failed to load the Config file");
            e.printStackTrace();
        }
    }

    public void createDatabase(){
        influxDB = InfluxDBFactory.connect(configuration.get("InfluxDBURL"), configuration.get("InfluxDBUsername"), configuration.get("InfluxDBPassword"));
        influxDB.createDatabase(configuration.get("dbName"));
    }

    /**
     * Simply return UDRServiceURL from configuration file
     * @return
     */
    public String getUDREndpointURL() {
        return configuration.get("UDRServiceUrl");
    }
}
