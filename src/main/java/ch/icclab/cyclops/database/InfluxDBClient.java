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

package ch.icclab.cyclops.database;

import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.usecases.mcn.model.McnBillingModel;
import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.usecases.tnova.model.TnovaChargeList;
import ch.icclab.cyclops.usecases.tnova.model.TnovaChargeResponse;
import ch.icclab.cyclops.usecases.tnova.model.TnovaTSDBData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
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
    private ClientResource cr;

    final static Logger logger = LogManager.getLogger(InfluxDBClient.class.getName());
    private Load load = new Load();
    private String url;// = Load.configuration.get("InfluxDBURL");
    private String username;// = Load.configuration.get("InfluxDBUsername");
    private String password;// = Load.configuration.get("InfluxDBPassword");
    private String dbName;// = Load.configuration.get("dbName");

    public InfluxDBClient() {
        this.url = Loader.getSettings().getInfluxDBSettings().getUrl();
        this.username = Loader.getSettings().getInfluxDBSettings().getUser();
        this.password = Loader.getSettings().getInfluxDBSettings().getPassword();
        this.dbName = Loader.getSettings().getInfluxDBSettings().getDbName();
    }

    public InfluxDBClient(ClientResource mockCr) {
        this.url = Loader.getSettings().getInfluxDBSettings().getUrl();
        this.username = Loader.getSettings().getInfluxDBSettings().getUser();
        this.password = Loader.getSettings().getInfluxDBSettings().getPassword();
        this.dbName = Loader.getSettings().getInfluxDBSettings().getDbName();
    }

    /**
     * Saves the data into InfluxDB via INfluxDB Java Client
     * It either saves a rate (resource, rate, rate_policy) or
     * a CDR (user, usage, price).
     * <p/>
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
        url = Loader.getSettings().getInfluxDBSettings().getUrl();
        username = Loader.getSettings().getInfluxDBSettings().getUser();
        password = Loader.getSettings().getInfluxDBSettings().getPassword();
        dbName = Loader.getSettings().getInfluxDBSettings().getDbName();
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
        }
        for (int i = 0; i < points.size(); i++) {
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
                        .tag("resource", resource)
                        .field("rate", rate)
                        .field("rate_policy", rate_policy)
                        .build();
            }
            influxDB.write(dbName, "default", point);
        }
        return true;
    }

    public String getTnovaCharge(String parameterQuery, String from, String to) {
        url = Loader.getSettings().getInfluxDBSettings().getUrl();
        username = Loader.getSettings().getInfluxDBSettings().getUser();
        password = Loader.getSettings().getInfluxDBSettings().getPassword();
        dbName = Loader.getSettings().getInfluxDBSettings().getDbName();

        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
        TnovaTSDBData dataObj = null;
        ObjectMapper mapper = new ObjectMapper();
        Query query = new Query(parameterQuery, dbName);

        ArrayList<TnovaChargeResponse> tnovaChargeResponses;
        tnovaChargeResponses = new ArrayList<TnovaChargeResponse>();
        TnovaChargeResponse tnovaChargeResponse = new TnovaChargeResponse();
        JSONArray resultArray = new JSONArray(influxDB.query(query).getResults());
        TnovaChargeList tnovaChargeList = new TnovaChargeList();
        Gson gson = new Gson();
        JSONObject resultObject;

        try {
            if (resultArray != null) {
                if (resultArray.toString().equals("[{}]")) {
                    return resultArray.toString();
                } else {
                    resultObject = (JSONObject) resultArray.get(0);
                    JSONArray series = (JSONArray) resultObject.get("series");

                    dataObj = mapper.readValue(series.get(0).toString(), TnovaTSDBData.class);//TSDBData
                }
            }
            ArrayList<String> columns = dataObj.getColumns();

            for (ArrayList<Object> value : dataObj.getValues()) {
                tnovaChargeResponse.setFields(value, columns);
                tnovaChargeResponses.add(tnovaChargeResponse);
            }

        } catch (IOException e) {
            logger.error("Error while parsing the Json: " + e.getMessage());
        } catch (JSONException e) {
            logger.error("Error while parsing the Json: " + e.getMessage());
        }

        tnovaChargeList.setFrom(from);
        tnovaChargeList.setTo(to);
        tnovaChargeList.setCharges(tnovaChargeResponses);

        return gson.toJson(tnovaChargeList);
    }

    /**
     * Fetches the data from InfluxDB via HTTP
     * The resulting data is a JSON object which is mapped to a POJO class (TSDBData).
     * <p/>
     * <p/>
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
        url = Loader.getSettings().getInfluxDBSettings().getUrl();
        username = Loader.getSettings().getInfluxDBSettings().getUser();
        password = Loader.getSettings().getInfluxDBSettings().getPassword();
        dbName = Loader.getSettings().getInfluxDBSettings().getDbName();

        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
        JSONArray resultArray;
        JSONObject resultObj;
        TSDBData[] dataObj = null;
        Representation output;
        ObjectMapper mapper = new ObjectMapper();
        int timeIndex = -1;

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
                    return data;
                } else {
                    JSONObject obj = (JSONObject) resultArray.get(0);
                    JSONArray series = (JSONArray) obj.get("series");
                    //Replace key "values" with key "points" in whole series
                    for (int i = 0; i < series.length(); i++) {
                        String response = series.get(i).toString();
                        //logger.trace("DATA TSDBData getData(String query): response="+response);
                        response.replaceFirst("values", "points");
                        response = response.split("values")[0] + "points" + response.split("values")[1];
                        //logger.trace("DATA TSDBData getData(String query): response="+response);
                        series.put(i, new JSONObject(response));
                    }
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
        return dataObj[0];
    }

    public TSDBData getTnovaData(String parameterQuery) {
        //TODO: connection method
        url = Loader.getSettings().getInfluxDBSettings().getUrl();
        username = Loader.getSettings().getInfluxDBSettings().getUser();
        password = Loader.getSettings().getInfluxDBSettings().getPassword();
        dbName = Loader.getSettings().getInfluxDBSettings().getDbName();

        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
        JSONArray resultArray;
        JSONObject resultObj;
        TSDBData[] dataObj = null;
        Representation output;
        ObjectMapper mapper = new ObjectMapper();
        int timeIndex = -1;

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
                    return data;
                } else {
                    JSONObject obj = (JSONObject) resultArray.get(0);
                    JSONArray series = (JSONArray) obj.get("series");
                    //Replace key "values" with key "points" in whole series
                    for (int i = 0; i < series.length(); i++) {
                        String response = series.get(i).toString();
                        //logger.trace("DATA TSDBData getData(String query): response="+response);
                        response.replaceFirst("values", "points");
                        response = response.split("values")[0] + "points" + response.split("values")[1];
                        //logger.trace("DATA TSDBData getData(String query): response="+response);
                        series.put(i, new JSONObject(response));
                    }
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
                        point.set(timeIndex, (String) point.get(timeIndex));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO new method, parametrization for generate CDR
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
        ArrayList<String[]> result = new ArrayList<String[]>();
        String[] split = json.split(":\\[")[2].split("],\\[");
        split[0] = split[0].substring(1);
        split[split.length - 1] = split[split.length - 1].substring(0, split[split.length - 1].length() - 2);
        for (int i = 0; i < split.length; i++) {
            result.add(split[i].split(","));
        }
        return result;
    }

    /**
     * Runs a query on InfluxDB. The first parameter is the query string. The second
     * is the database.
     *
     * @param params
     * @return
     */
    public TSDBData[] query(String... params) {
        logger.trace("BEGIN query TSDBData[] query(String parameterQuery)");
        String parameterQuery = params.length > 0 ? params[0] : "";
        String dbname = params.length > 1 ? params[1] : Loader.getSettings().getInfluxDBSettings().getDbName();

        InfluxDB influxDB = InfluxDBFactory.connect(this.url, this.username, this.password);
        TSDBData[] tsdbData = null;
        JSONArray resultArray = null;
        TSDBData[] dataObj = null;
        ObjectMapper mapper = new ObjectMapper();

        Query query = new Query(parameterQuery, dbname);
        try {
            logger.debug("Attempting to execute the query: " + parameterQuery + " into the db: " + dbname);
            resultArray = new JSONArray(influxDB.query(query).getResults());
            logger.debug("Obtained results: " + resultArray.toString());
            if (!resultArray.isNull(0)) {
                if (resultArray.toString().equals("[{}]")) {
                    logger.debug("Result is [{}]");
                    TSDBData data = new TSDBData();
                    data.setColumns(new ArrayList<String>());
                    data.setPoints(new ArrayList<ArrayList<Object>>());
                    data.setTags(new HashMap());
                    tsdbData = new TSDBData[1];
                    tsdbData[0] = data;
                } else {
                    JSONObject obj = (JSONObject) resultArray.get(0);
                    //TODO: translate data format
                    logger.debug("JSON obj: " + obj.toString());
                    JSONArray series = (JSONArray) obj.get("series");
                    for (int i = 0; i < series.length(); i++) {
                        String respons = series.get(i).toString();
                        respons = respons.split("values")[0] + "points" + respons.split("values")[1];
                        series.put(i, new JSONObject(respons));
                    }
                    dataObj = mapper.readValue(series.toString(), TSDBData[].class);
                    logger.debug("dataObj: " + dataObj.toString());
                    tsdbData = dataObj;
                }
            } else {
                logger.debug("Result is null");
                TSDBData data = new TSDBData();
                data.setColumns(new ArrayList<String>());
                data.setPoints(new ArrayList<ArrayList<Object>>());
                data.setTags(new HashMap());
                tsdbData = new TSDBData[1];
                tsdbData[0] = data;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.trace("END query TSDBData[] query(String parameterQuery)");
        return tsdbData;
    }

    /**
     * Asks for InfluxDB BatchPoints container
     *
     * @return empty container
     */
    public BatchPoints giveMeEmptyContainer() {
        return BatchPoints
                .database(dbName)
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
    }

    /**
     * Ask for connection to InfluxDB
     *
     * @return
     */
    private InfluxDB getConnection() {
        return InfluxDBFactory.connect(this.url, this.username, this.password);
    }

    /**
     * Save container to InfluxDB container
     *
     * @param container that is goint to be saved
     */
    public void saveContainerToDB(BatchPoints container) {
        InfluxDB db = getConnection();
        db.write(container);
    }

    /**
     * Will look into database and return the date of last pull, or epoch if there was none
     *
     * @return String
     */
    public String getLastPull() {
        InfluxDB db = getConnection();

        // this is epoch
        String date = "";


        String select = "";

        // prepare select query
        if (Loader.getEnvironment().equals("Tnova")) {
            select = "select * from tnova_cdr order by desc limit 1";
            //TODO: create Configuration file property to get the table name
        } else if (Loader.getEnvironment().equals("Mcn")) {
            select = "select * from mcn_cdr order by desc limit 1";

        }

        try {

            // fire up the query
            QueryResult response = db.query(createQuery(select, dbName));

            // parse all results
            JSONArray resultArray = new JSONArray(response.getResults());

            // get first result
            JSONObject result = (JSONObject) resultArray.get(0);

            // get series
            JSONArray series = (JSONArray) result.get("series");

            // get first series
            JSONObject firstSeries = (JSONObject) series.get(0);

            // get values
            JSONArray values = (JSONArray) firstSeries.get("values");

            // use first value
            JSONArray firstValue = (JSONArray) values.get(0);

            // and finally our time
            date = firstValue.get(0).toString().replace("Z", "");

        } catch (Exception e) {
            logger.debug("This is the first pull, therefore we will pull data since Epoch");
        }

        return date;
    }

    /**
     * Creates query for provided command and db
     *
     * @param command
     * @param db
     * @return Query
     */
    protected Query createQuery(String command, String db) {
        return new Query(command, db);
    }

    /**
     * Retrieve Billing Model from the DB
     *
     * @param customer
     * @param resource
     * @param time
     */
    public McnBillingModel getBillingModel(String customer, String resource, String time) {
        McnBillingModel billingModel = new McnBillingModel();

        billingModel.setPrice(getRate(resource, time));

        return billingModel;
    }

    private double getRate(String resource, String timeFrom) {
        String query = getRateQuery(resource, timeFrom);
        TSDBData tsdbData = this.getData(query);
        int rateIndex = -1;
        double rate;
        ArrayList<String> columns = tsdbData.getColumns();
        ArrayList<ArrayList<Object>> points = tsdbData.getPoints();

        //Depending on the result of the first query, if we don't have a rate for that period, we use the last rate on the db.
        if (points.size() < 1) {
            query = this.getLastRateQuery(resource);
            tsdbData = this.getData(query);
            columns = tsdbData.getColumns();
            points = tsdbData.getPoints();
        }
        //If there is an entry before we generate a rate
//        if (points.size() > 0
// ) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).equals("rate"))
                rateIndex = i;
        }
        rate = Double.parseDouble((String) points.get(0).get(rateIndex));
//        }
//        else{
//
//        }
        return rate;
    }

    private String getRateQuery(String resource, String timeFrom) {
        return "SELECT rate FROM rate WHERE resource='" + resource + "' AND time>'" + timeFrom + "'";
    }

    private String getLastRateQuery(String resource) {
        return "SELECT rate FROM rate WHERE resource='" + resource + "' ORDER BY time DESC limit 1";
    }

    private String getLastRatesAllResourcesQuery() {
        return "SELECT rate FROM rate GROUP BY resource ORDER BY time DESC LIMIT 1";
    }

    /**
     * Create databases based on list of names
     *
     * @param names for database creation
     */
    public void createDatabases(String... names) {
        InfluxDB client = getConnection();

        // now create required databases
        for (String name : names) {
            client.createDatabase(name);
        }
    }

    /**
     * Ask influxDB to query database for me
     *
     * @param query string
     * @return QueryResult
     */
    public QueryResult runQuery(String query) {
        InfluxDB db = getConnection();
        return db.query(new Query(query, dbName));
    }

}
