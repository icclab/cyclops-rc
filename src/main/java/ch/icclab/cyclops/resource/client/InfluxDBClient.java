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

package ch.icclab.cyclops.resource.client;

import ch.icclab.cyclops.model.TSDBData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import ch.icclab.cyclops.util.Load;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Author: Srikanta
 * Created on: 15-Oct-14
 * Description: Client class for InfluxDB
 */
public class InfluxDBClient extends ClientResource {
    private String dbUsername;
    private String dbPassword;
    private ClientResource cr;

    final static Logger logger = LogManager.getLogger(InfluxDBClient.class.getName());
    private Load load = new Load();
    private String url;// = Load.configuration.get("InfluxDBURL");
    private String username;// = Load.configuration.get("InfluxDBUsername");
    private String password;// = Load.configuration.get("InfluxDBPasswordy  s");
    private String dbName;// = Load.configuration.get("dbName");

    public InfluxDBClient() {
        this.url = Load.configuration.get("InfluxDBURL");
        this.dbUsername = Load.configuration.get("InfluxDBUsername");
        this.dbPassword = Load.configuration.get("InfluxDBPassword");
        this.username = Load.configuration.get("InfluxDBUsername");
        this.password = Load.configuration.get("InfluxDBPassword");
        this.dbName = Load.configuration.get("dbName");
    }

    public InfluxDBClient(ClientResource mockCr) {
        this.url = Load.configuration.get("InfluxDBURL");
        this.dbUsername = Load.configuration.get("InfluxDBUsername");
        this.dbPassword = Load.configuration.get("InfluxDBPassword");
        this.username = Load.configuration.get("InfluxDBUsername");
        this.password = Load.configuration.get("InfluxDBPassword");
        this.dbName = Load.configuration.get("dbName");
    }

    /**
     * Saves the data into InfluxDB via INfluxDB Java Client
     * It either saves a rate (resource, rate, rate_policy) or
     * a CDR (user, usage, price).
     * <p>
     * Pseudo Code
     * 1. Load the login credentials from the configuration object
     * 2. Create a client instance and set the url and auth details
     * 3. Create data points from data string and save it to InfluxDB client
     *
     * @param data
     * @return boolean
     */
    public boolean saveData(String data) {
        //TODO: check parametrization of this method
        //TODO: method getConnection() for InfluxDB
        url = load.configuration.get("InfluxDBURL");
        username = load.configuration.get("InfluxDBUsername");
        password = load.configuration.get("InfluxDBPassword");
        dbName = load.configuration.get("dbName");
        //url = (url+"?writeDb="+dbName);
        logger.trace("BEGIN boolean saveData(String data)");
        logger.trace("DATA boolean saveData(String data): data=" + data);
        logger.trace("DATA boolean saveData(String data): url=" + url);
        //data = "[" + data + "]";
        //System.out.println(data);
        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
        Representation output;
        //TODO: small POJO object instead of String
        String[] columns = getColumns(data);
        ArrayList<String[]> points = getPoints(data);
        //TODO: parametrization: either save a rate object or a charge object
        //TODO: create a findIndex method
        int resourceIndex = -1;
        int rateIndex = -1;
        int rate_policyIndex = -1;
        int usageIndex = -1;
        int priceIndex = -1;
        int useridIndex = -1;
        for (int i = 0; i < columns.length; i++) {

            if (columns[i].equals("resource"))
                resourceIndex = i;
            else if (columns[i].equals("rate"))
                rateIndex = i;
            else if (columns[i].equals("rate_policy"))
                rate_policyIndex = i;
            else if (columns[i].equals("usage"))
                usageIndex = i;
            else if (columns[i].equals("price"))
                priceIndex = i;
            else if (columns[i].equals("userid"))
                useridIndex = i;
            logger.trace("DATA boolean saveData(String data): resourceIndex=" + resourceIndex);
            logger.trace("DATA boolean saveData(String data): rateIndex=" + rateIndex);
            logger.trace("DATA boolean saveData(String data): rate_policyIndex=" + rate_policyIndex);
            logger.trace("DATA boolean saveData(String data): usageIndex=" + usageIndex);
            logger.trace("DATA boolean saveData(String data): priceIndex=" + priceIndex);
            logger.trace("DATA boolean saveData(String data): useridIndex=" + useridIndex);
        }
        logger.trace("DATA boolean saveData(String data): points.size=" + points.size());
        for (int i = 0; i < points.size(); i++) {
            logger.trace("DATA boolean saveData(String data): i=" + i);
            logger.trace("DATA boolean saveData(String data): points.size=" + points.size());
            logger.trace("DATA boolean saveData(String data): data=" + data);
            logger.trace("DATA boolean saveData(String data): resourceIndex=" + points.get(i)[resourceIndex]);

            logger.trace("DATA boolean saveData(String data): point.length=" + points.get(i).length);
            Point point;
            if ((rate_policyIndex == -1) && (rateIndex == -1)) {
                String price = String.valueOf(points.get(i)[priceIndex]);
                if (price.contains("]")) {
                    price = price.replace("]", "");
                }
                String userid = points.get(i)[useridIndex].replace("\"", "");
                String usage = points.get(i)[usageIndex].replace("\"", "");
                String resource = points.get(i)[resourceIndex].replace("\"", "");
                logger.trace("DATA boolean saveData(String data): userid=" + userid);
                point = Point.measurement(data.split("name\":\"")[1].split("\"")[0])
                        .field("resource", resource)
                        .field("usage", usage)
                        .field("price", price)
                        .tag("userid", userid)
                        .build();
            } else {
                String resource = points.get(i)[resourceIndex];
                String rate = points.get(i)[rateIndex];
                String rate_policy = points.get(i)[rate_policyIndex];
                //TODO: create string cleaner method
                rate = rate.replace("]", "");
                rate = rate.replace("\"", "");
                rate_policy = rate_policy.replace("]", "");
                rate_policy = rate_policy.replace("\"", "");
                resource = resource.replace("\"", "");
                point = Point.measurement(data.split("name\":\"")[1].split("\"")[0])
                        .field("resource", resource)
                        .field("rate", rate)
                        .field("rate_policy", rate_policy)
                        .build();
            }
            logger.trace("DATA boolean saveData(String data): point=" + point);
            influxDB.write(dbName, "default", point);
        }


        logger.trace("END boolean saveData(String data)");
        return true;
    }


    /**
     * Fetches the data from InfluxDB via HTTP
     * The resulting data is a JSON object which is mapped to a POJO class (TSDBData).
     * <p>
     * <p>
     * Pseudo Code
     * 1. Load the login credentials from the configuration object
     * 2. Create a client instance and set the HTTP protocol, url and auth details
     * 3. Query the db through its API
     * 4. Convert the json response into a TSDB java object
     *
     * @param parameterQuery A query string
     * @return TSDBData
     */
    public TSDBData getData(String parameterQuery) {
        //TODO: connection method
        logger.trace("BEGIN TSDBData getData(String parameterQuery)");

        url = load.configuration.get("InfluxDBURL");
        username = load.configuration.get("InfluxDBUsername");
        password = load.configuration.get("InfluxDBPassword");
        dbName = load.configuration.get("dbName");

        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
        JSONArray resultArray;
        JSONObject resultObj;
        TSDBData[] dataObj = null;
        Representation output;
        ObjectMapper mapper = new ObjectMapper();
        int timeIndex = -1;

        logger.trace("DATA TSDBData getData(String parameterQuery): parameterQuery=" + parameterQuery);
        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        Query query = new Query(parameterQuery, dbName);

        try {
            resultArray = new JSONArray(influxDB.query(query).getResults());
            String sad = resultArray.toString();
            if (!resultArray.isNull(0)) {
                if (resultArray.toString().equals("[{}]")) {
                    //resultArray is an empty JSON string
                    // --> return an empty TSDBData POJO object
                    TSDBData data = new TSDBData();
                    data.setColumns(new ArrayList<String>());
                    data.setPoints(new ArrayList<ArrayList<Object>>());
                    data.setTags(new HashMap());
                    logger.trace("DATA TSDBData getData(String parameterQuery): data=" + data);
                    return data;
                } else {
                    JSONObject obj = (JSONObject) resultArray.get(0);
                    logger.trace("DATA TSDBData getData(String query): obj=" + obj.toString());
                    JSONArray series = (JSONArray) obj.get("series");
                    logger.trace("DATA TSDBData getData(String query): series=" + series.toString());
                    //Replace key "values" with key "points" in whole series
                    for (int i = 0; i < series.length(); i++) {
                        String response = series.get(i).toString();
                        //logger.trace("DATA TSDBData getData(String query): response="+response);
                        response.replaceFirst("values", "points");
                        response = response.split("values")[0] + "points" + response.split("values")[1];
                        //logger.trace("DATA TSDBData getData(String query): response="+response);
                        series.put(i, new JSONObject(response));
                    }

                    logger.trace("DATA TSDBData getData(String query): series=" + series.toString());
                    logger.trace("DATA TSDBData getData(String query): series=" + series.get(0).toString());
                    dataObj = mapper.readValue(series.toString(), TSDBData[].class);
                }
            }
            //Filter the points to format the date
            for (int i = 0; i < dataObj.length; i++) {
                for (int o = 0; o < dataObj[i].getColumns().size(); o++) {
                    if (dataObj[i].getColumns().get(o).equalsIgnoreCase("time"))
                        timeIndex = o;
                }
                if (timeIndex > -1) {
                    TreeMap<String, ArrayList> points = new TreeMap<String, ArrayList>();
                    for (ArrayList point : dataObj[i].getPoints()) {
                        point.set(timeIndex, formatDate((String) point.get(timeIndex)));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO new method, parametrization for generate CDR
        logger.trace("DATA TSDBData getData(String parameterQuery): data=" + dataObj);
        logger.trace("END TSDBData getData(String parameterQuery)");
        return dataObj[0];
    }

    /**
     * Converts a datetime string into a number.
     *
     * @param dateAndTime
     * @return Long
     */
    private Long formatDate(String dateAndTime) {
        logger.trace("BEGIN Long formatDate(String dateAndTime)");
        Date result = null;
        try {
            String date = dateAndTime.split("T")[0];
            String hour = dateAndTime.split("T")[1];
            hour = hour.substring(0, 8);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result = formatter.parse(date + " " + hour);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.trace("END Long formatDate(String dateAndTime)");
        return result.getTime();
    }

    private String[] getColumns(String json) {
        //TODO: replace this method with small POJO object that contains column names
        logger.trace("BEGIN String[] getColumns(String json)");
        String[] result = json.split(":\\[")[1].split("]")[0].split(",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].substring(1, result[i].length() - 1);
        }
        logger.trace("END String[] getColumns(String json)");
        return result;
    }

    private ArrayList<String[]> getPoints(String json) {
        logger.trace("BEGIN ArrayList<String[]> getPoints(String json)");
        ArrayList<String[]> result = new ArrayList<String[]>();
        String[] split = json.split(":\\[")[2].split("],\\[");
        logger.trace("DATA ArrayList<String[]> getPoints(String json): split=" + Arrays.toString(split));
        if (!json.contains("\"points\":[]")) {
            split[0] = split[0].substring(1);
            split[split.length - 1] = split[split.length - 1].substring(0, split[split.length - 1].length() - 15);
            logger.trace("DATA ArrayList<String[]> getPoints(String json): split=" + Arrays.toString(split));
            for (int i = 0; i < split.length; i++) {
                String[] entry = split[i].split(",");
                for (int o = 0; o < entry.length; o++) {
                    entry[o] = entry[o].replace("\"", "");
                }
                result.add(entry);
                logger.trace("DATA ArrayList<String[]> getPoints(String json): result=" + split[i]);
            }
            logger.trace("DATA ArrayList<String[]> getPoints(String json): split=" + Arrays.toString(split));
            logger.trace("END ArrayList<String[]> getPoints(String json)");
        }
        return result;
    }
}
