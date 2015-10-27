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

package ch.icclab.cyclops.resource.impl;

import ch.icclab.cyclops.model.RateEngineResponse;
import ch.icclab.cyclops.model.ResourceUsage;
import ch.icclab.cyclops.model.TSDBData;
import ch.icclab.cyclops.resource.client.AccountingClient;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import ch.icclab.cyclops.resource.client.RuleEngineClient;
import ch.icclab.cyclops.resource.client.UDRServiceClient;
import ch.icclab.cyclops.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import ch.icclab.cyclops.util.DateTimeUtil;
import ch.icclab.cyclops.util.Flag;
import ch.icclab.cyclops.util.Load;

import java.io.IOException;
import java.util.*;

/**
 * Author: Srikanta
 * Created on: 25-Mar-15
 * Description: The class creates the charge data records and rate for resources when invoked.
 *
 */
public class GenerateResource extends ServerResource {
    final static Logger logger = LogManager.getLogger(GenerateResource.class.getName());

    private String action;
    private ArrayList enabledResourceList;
    private UDRServiceClient udrServiceClient;
    private AccountingClient accountingClient;
    private InfluxDBClient dbClient;
    private Load load = new Load();
    private String url = load.configuration.get("InfluxDBURL");
    private String username = load.configuration.get("InfluxDBUsername");
    private String password = load.configuration.get("InfluxDBPassword");
    private InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
    private String dbname = load.configuration.get("dbName");

    public GenerateResource() {
        logger.trace("BEGIN CONSTRUCTOR GenerateResource()");
        this.enabledResourceList = new ArrayList();
        this.udrServiceClient = new UDRServiceClient();
        this.accountingClient = new AccountingClient();
        this.load = new Load();
        this.dbClient = new InfluxDBClient();
        logger.trace("END CONSTRUCTOR GenerateResource()");
    }

    public GenerateResource(String mockAction, InfluxDBClient mockDbClient, UDRServiceClient mockUdrServiceClient){
        logger.trace("BEGIN CONSTRUCTOR GenerateResource(String mockAction, InfluxDBClient mockDbClient, UDRServiceClient mockUdrServiceClient)");
        this.action = mockAction;
        this.dbClient = mockDbClient;
        this.udrServiceClient = mockUdrServiceClient;
        this.enabledResourceList = new ArrayList();
        this.load = new Load();
        logger.trace("END CONSTRUCTOR GenerateResource(String mockAction, InfluxDBClient mockDbClient, UDRServiceClient mockUdrServiceClient)");
    }

    public GenerateResource(UDRServiceClient udrServiceClient) {
        logger.trace("BEGIN CONSTRUCTOR GenerateResource(UDRServiceClient udrServiceClient)");
        this.udrServiceClient = udrServiceClient;
        this.enabledResourceList = new ArrayList();
        this.load = new Load();
        logger.trace("END CONSTRUCTOR GenerateResource(UDRServiceClient udrServiceClient)");
    }

    /**
     * The initial method to be executed when the class is invoked. The request parameter is extracted to a variable
     *
     * @return void
     */
    @Override
    public void doInit() {
        action = (String) getRequestAttributes().get("action");
    }

    /**
     * Checks for the request attribute and invoke the corresponding method for business logic
     *
     * @return String
     */
    @Get
    public String serviceRequest() throws IOException, JSONException {
        logger.trace("BEGIN String serviceRequest() throws IOException, JSONException");
        boolean rateResult = false;
        boolean cdrResult = false;
        boolean tcdrResult = false;

        // Get the list of enabled resources
        enabledResourceList = getEnabledResources();
        // Service the request
        logger.trace("DATA String serviceRequest() throws IOException, JSONException: enabledResourceList="+enabledResourceList);
        logger.trace("DATA String serviceRequest() throws IOException, JSONException: action="+action);
        if (action.equalsIgnoreCase("rate")) {
            rateResult = generateRate();
        } else if (action.equalsIgnoreCase("cdr")) {
            cdrResult = generateCdr();
        } else if (action.equalsIgnoreCase("t-cdr")) {
            tcdrResult = generateTNovaCdr();
        }
        // Construct the response
        if (rateResult) {
            logger.trace("END String serviceRequest() throws IOException, JSONException");
            return "The rate generation was successful";
        } else if (cdrResult) {
            logger.trace("END String serviceRequest() throws IOException, JSONException");
            return "The cdr generation was successful";
        } else if (tcdrResult) {
            logger.trace("END String serviceRequest() throws IOException, JSONException");
            return "The T-Nova cdr generation was successful";
        } else {
            logger.debug("DEBUG String serviceRequest() throws IOException, JSONException: Operation failed");
            return "Operation Failed";
        }
    }

    /**
     * Get a list of meters which are selected
     *
     * Pseudo Code
     * 1. Connect to the UDR service to get a list of meters
     * 2. Store the meters which are selected
     *
     * @return ArrayList List containing the meters which are selected
     */
    private ArrayList getEnabledResources() {
        logger.trace("BEGIN ArrayList getEnabledResources()");
        ArrayList enabledResourceList = new ArrayList();
        String meterData;
        JsonRepresentation responseJson;
        int meterNameIndex = 0;
        int meterStatusIndex = 0;
        JSONArray columnArr, tagArr, pointsArr;
        // Get the active meters from the meter API from UDR Service
        try {
            logger.info("INFO ArrayList getEnabledResources(): Opening UDR Service Client");
            meterData = udrServiceClient.getActiveResources();
            logger.info("DATA ArrayList getEnabledResources(): meterData=" + meterData.toString());
            responseJson = new JsonRepresentation(meterData);
            logger.info("DATA ArrayList getEnabledResources(): responseJson=" + responseJson.toString());
            JSONObject jsonObj = responseJson.getJsonObject();
            columnArr = jsonObj.getJSONArray("columns");
            //tagArr = jsonObj.getJSONArray("tags");
            logger.trace("DATA ArrayList getEnabledResources(): responseJson=" + responseJson.toString());
            for (int i = 0; i < columnArr.length(); i++) {
                if ("metername".equals(columnArr.get(i))) {
                    meterNameIndex = i;
                }
                if ("status".equals(columnArr.get(i))) {
                    meterStatusIndex = i;
                }
            }
            logger.info("DATA ArrayList getEnabledResources(): jsonObj" + jsonObj);
            pointsArr = jsonObj.getJSONArray("points");
            HashMap<String, String> enabledResourceMap = new HashMap<String, String>();
            for (int j = 0; j < pointsArr.length(); j++) {
                JSONArray arr = (JSONArray) pointsArr.get(j);
                if (Integer.parseInt(arr.get(meterStatusIndex).toString()) == 1) {
                    if (!enabledResourceList.contains(arr.get(meterNameIndex)))
                        enabledResourceList.add(arr.get(meterNameIndex));
                }
            }
        } catch (IOException e) {
            logger.error("EXCEPTION IOEXCEPTION ArrayList getEnabledResources()");
            e.printStackTrace();
        } catch (JSONException e) {
            logger.error("EXCEPTION JSONEXCEPTION ArrayList getEnabledResources()");
            e.printStackTrace();
        }
        logger.info("DATA ArrayList getEnabledResources(): enabledResourceList" + enabledResourceList);
        logger.trace("END ArrayList getEnabledResources()");
        return enabledResourceList;
    }

    /**
     * Initiated the generation of rate depending on the existing rating policy
     *
     * Pseudo Code
     * 1. Check for the rating policy
     * 2. Invoke the method to initiate the rate generation
     *
     * @return boolean
     */
    private boolean generateRate(){
        logger.trace("BEGIN boolean generateRate()");
        TSDBData rateObj;
        boolean result = false;

        if(Flag.getMeteringType().equalsIgnoreCase("static")){
            rateObj = generateStaticRate();
        }else{
            rateObj = generateDynamicRate();
        }
        //System.out.println("Generated rate obj" + rateObj.getPoints().toString());
        result = saveRate(rateObj);
        logger.trace("END boolean generateRate()");
        return result;
    }

    /**
     * Saves a TSDB dataObj into the database
     *
     * Pseudo Code
     * 1. Create a TSDB client object (POJO object)
     * 2. Convert the data object into a json string
     * 3. Save the string into the database
     *
     * @param rateObj A TSDBData object containing the content to be saved into the db
     * @return boolean
     */
    private boolean saveRate(TSDBData rateObj) {
        logger.trace("BEGIN boolean saveRate(TSDBData rateObj)");
        InfluxDBClient dbClient = new InfluxDBClient();

        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;
        boolean result;
        try {
            jsonData = mapper.writeValueAsString(rateObj);
        } catch (JsonProcessingException e) {
            logger.error("EXCEPTION JSONPROCESSINGEXCEPTION boolean saveRate(TSDBData rateObj)");
            e.printStackTrace();
        }
        logger.trace("DATA boolean saveRate(TSDBData rateObj): jsonData="+jsonData);
        System.out.println("Generated save rate into db " + jsonData);
        result = dbClient.saveData(jsonData);
        System.out.println("Result " + result);
        logger.trace("END boolean saveRate(TSDBData rateObj)");
        return result;
    }

    /**
     * Generates the static rate for a resource
     *
     * Pseudo Code
     * 1. Get the current list of resources along with their static rates
     * 2. Create a dataObj consisting of the resources along with its resources.
     *
     * @return TSDBData Data obj to be saved into the db
     */
    private TSDBData generateStaticRate() {
        //TODO: check if there is generic method to to return rateData
        logger.trace("BEGIN TSDBData generateStaticRate()");
        //ArrayList<Object> objArrNode;
        //Iterator<Map.Entry<String, Object>> entries;
        //Object key;

        //ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
       // TSDBData rateData = new TSDBData();
/*
        entries = load.getStaticRate().entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<String, Object> entry = entries.next();
            key = entry.getKey();
            objArrNode = new ArrayList<Object>();
            objArrNode.add(key);
            objArrNode.add(entry.getValue());
            objArrNode.add("static");
            objArr.add(objArrNode);
        }*/

        // Set the data object, so it can be saved later in DB
        /*rateData.setName("rate");
        rateData.setColumns(strArr);
        rateData.setPoints(objArr);*/

        ArrayList<String> strArr = StringUtil.strArr("resource","rate","rate_policy");
        TSDBData rateData = createPOJOObject("rate", strArr, load.getStaticRate());
        logger.trace("DATA TSDBData generateStaticRate(): rateData=" + rateData);
        logger.trace("END TSDBData generateStaticRate()");
        return  rateData;
    }

    private TSDBData createPOJOObject(String name, ArrayList<String> columns, HashMap entrySet){
        ArrayList<Object> objArrNode;
        Iterator<Map.Entry<String, Object>> entries;
        Object key;
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        TSDBData result = new TSDBData();

        //TODO: parametrization of method
        entries = entrySet.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<String, Object> entry = entries.next();
            key = entry.getKey();
            objArrNode = new ArrayList<Object>();
            objArrNode.add(key);
            objArrNode.add(entry.getValue());
            objArrNode.add("static");
            objArr.add(objArrNode);
        }

        result.setName(name);
        result.setColumns(columns);
        result.setPoints(objArr);

        return result;
    }

    /**
     * Generates the dynamic rate for a resource
     *
     * Pseudo Code
     * 1. For every resources, create a rate through a random function with the value between a range.
     *
     * @return TSDBData Data obj to be saved into the db
     */
    private TSDBData generateDynamicRate(){
        //TODO: replace with single method generateRate and parameter (static, dynamic)
        logger.trace("BEGIN TSDBData generateDynamicRate()");
        double rate;
        Random rateGenerator = new Random();
        TSDBData rateData = new TSDBData();
        ArrayList<String> strArr = null;
        ArrayList<Object> objArrNode;
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();

        // Iterate through the list and generate the rate for each enabled meters
        for(int k=0;k<enabledResourceList.size();k++){
            //rate = getDynamicRate(enabledResourceList.get(k).toString());
            rate = rateGenerator.nextInt((3 - 1) + 1) + 1;
            rate = 0.25 + rateGenerator.nextDouble() * 1;
            strArr = StringUtil.strArr("resource","rate","rate_policy");
            //strArr.add("resource");
            //strArr.add("rate");
            //strArr.add("rate_policy");
            objArrNode = new ArrayList<Object>();
            objArrNode.add(enabledResourceList.get(k));
            objArrNode.add(rate);
            objArrNode.add(Flag.getMeteringType());
            objArr.add(objArrNode);
        }

        rateData.setName("rate");
        rateData.setColumns(strArr);
        rateData.setPoints(objArr);
        logger.trace("DATA TSDBData generateDynamicRate(): rateData="+rateData);
        logger.trace("END TSDBData generateDynamicRate()");
        return rateData;
    }

    /**
     * Request for generating the dynamic rate from the rule engine
     *
     * Pseudo Code
     * 1. Connect to the rule engine
     * 2. Invoke the getRate method with the resource name
     * 3. Return the generated rate
     *
     * @param resourceName String containing the name of the resource for which the rate needs to be generated
     * @return double The rate generated by the rule engine
     */
    //TODO: check why this is not used yet. RuleEngine has to be implemented
    private double getDynamicRate(String resourceName) {
        logger.trace("BEGIN double getDynamicRate(String resourceName)");
        double rate;
        RateEngineResponse response;
        RuleEngineClient client = new RuleEngineClient();

        response = client.getRate(resourceName);
        rate = response.getRate();
        //System.out.println("Got the response from rule engine. Rate: " + response.getRate());
        logger.trace("END double getDynamicRate(String resourceName)");
        return rate;
    }

    /**
     * Generate the CDR of all the users for the selected meters
     *
     * Pseudo Code
     * 1. Get the list of selected meters
     * 2. Query the UDR service to get the usage information under these meters for all the users for a time period
     * 3. Get the rate for the same meters for a same time period
     * 4. Combine the rate and usage to get the charge value
     * 5. Save it in the db
     *
     * @return boolean
     */
    private boolean generateCdr() throws IOException, JSONException {
        //TODO: split it into smaller methods
        logger.trace("BEGIN boolean generateCdr() throws IOException, JSONException");
        Object usage;
        double charge;
        String from, to;
        String[] time;
        int indexUserId, indexUsage;
        ArrayList usageListArr, usageArr;
        ArrayList columnArr;
        UDRServiceClient udrClient = new UDRServiceClient();
        RateResource rateResource = new RateResource();
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        Double rate;
        TSDBData tsdbData;
        boolean result;
        String userid;
        ArrayList<ResourceUsage> resourceUsageArray;
        ArrayList<Object> objArrNode;
        HashMap tags;

        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        time = dateTimeUtil.getRange();

        from = time[1];
        to = time[0];
        logger.trace("DATA boolean generateCdr() throws IOException, JSONException: enabledResourceList"+enabledResourceList);
        for (int i = 0; i < enabledResourceList.size(); i++) {
            tsdbData = rateResource.getResourceRate(enabledResourceList.get(i).toString(), from, to);
            rate = calculateRate(tsdbData);
            resourceUsageArray = udrClient.getResourceUsageData(enabledResourceList.get(i).toString(), from, to);
            logger.trace("DATA boolean generateCdr() throws IOException, JSONException: resourceUsageStr" + resourceUsageArray);
            for(ResourceUsage resource : resourceUsageArray){
                columnArr = resource.getColumn();
                logger.trace("DATA boolean generateCdr()...: columnArr=" + Arrays.toString(columnArr.toArray()));
                usageListArr = resource.getUsage();
                tags = resource.getTags();
                logger.trace("DATA boolean generateCdr()...: tags=" + tags);
                //indexUserId = columnArr.indexOf("userid");
                userid = tags.get("userid").toString();
                //userid = userid.substring(0, userid.length());
                logger.trace("DATA boolean generateCdr()...: userid=" + userid);
                logger.trace("DATA boolean generateCdr()...: usageListArr=" + usageListArr.toString());
                indexUsage = columnArr.indexOf("mean");
                // The below if condition differentiates between the gauge and cumulative meters of openstack
                if (indexUsage < 0) {
                    indexUsage = columnArr.indexOf("usage");//usage if we are not using the sum in the sql else "sum"
                }
                // Iterate through the usage arraylist to extract the userid and usage.
                // Multiple the usage with the rate of the resource and save it into an arraylist
                for (int j = 0; j < usageListArr.size(); j++) {
                    usageArr = (ArrayList) usageListArr.get(j);
                    for (int k = 0; k < usageListArr.size(); k++) {//resourceUsageStr.get(usage).size()
                        objArrNode = new ArrayList<Object>();
                        //userid = (String) usageArr.get(indexUserId);
                        usage = usageArr.get(indexUsage);
                        // Calculate the charge for a resource per user
                        Double d = Double.parseDouble(usage.toString());
                        charge = (d * rate);
                        objArrNode.add(enabledResourceList.get(i).toString());
                        objArrNode.add(userid);
                        objArrNode.add(usage);
                        objArrNode.add(charge);
                        objArr.add(objArrNode);
                    }
                }
            }

        }
        // Save the charge array into the database
        result = savePrice(objArr);
        logger.trace("END boolean generateCdr() throws IOException, JSONException");
        return result;
    }


    /**
     * Generate the CDR for T-Nova service
     *
     * Pseudo Code
     * 1. Get the list of selected meters
     * 2. Query the UDR service to get the usage information under these meters for all the users for a time period
     * 3. Get the rate for the same meters for a same time period
     * 4. Combine the rate and usage to get the charge value
     * 5. Save it in the db
     *
     * @return boolean
     */
    private boolean generateTNovaCdr() throws IOException, JSONException {
        logger.trace("BEGIN boolean generateCdr() throws IOException, JSONException");
        Object usage;
        double charge;
        String from, to;
        String[] time;
        int indexUserId, indexUsage;
        ArrayList usageListArr, usageArr;
        ArrayList columnArr;
        UDRServiceClient udrClient = new UDRServiceClient();
        RateResource rateResource = new RateResource();
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        Double rate;
        TSDBData tsdbData;
        boolean result;
        String userid;
        String service_instance_id;
        ResourceUsage resourceUsageStr;
        ArrayList<Object> objArrNode;
        HashMap tags;

        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        time = dateTimeUtil.getRange();

        from = time[1];
        to = time[0];
        logger.trace("DATA boolean generateCdr() throws IOException, JSONException: enabledResourceList"+enabledResourceList);
        for(int i=0; i<enabledResourceList.size(); i++){
            tsdbData = rateResource.getResourceRate(enabledResourceList.get(i).toString(), from, to);
            //ToDo: rate tsdbData needs a field service_instance_id
            rate = calculateRate(tsdbData);
            resourceUsageStr = udrClient.getResourceUsageData(enabledResourceList.get(i).toString(), from, to).get(0);



            //TODO: KoBe, I added .get(0) but you have to check if the iteration still does what you want because the getResourceUsageData now is retrieving different
            // Nothing else has been changed from your code for TNovaCDR



            logger.trace("DATA boolean generateCdr() throws IOException, JSONException: resourceUsageStr"+resourceUsageStr);
            columnArr = resourceUsageStr.getColumn();
            logger.trace("DATA boolean generateCdr()...: columnArr=" + Arrays.toString(columnArr.toArray()));
            usageListArr = resourceUsageStr.getUsage();
            tags = resourceUsageStr.getTags();
            logger.trace("DATA boolean generateCdr()...: tags=" + tags);
            indexUserId = columnArr.indexOf("userid");
            userid = tags.get("userid").toString();
            logger.trace("DATA boolean generateCdr()...: userid=" + userid);
            logger.trace("DATA boolean generateCdr()...: usageListArr=" + usageListArr.toString());
            indexUsage = columnArr.indexOf("mean");
            // The below if condition differentiates between the gauge and cumulative meters of openstack
            if(indexUsage < 0){
                indexUsage = columnArr.indexOf("sum");
            }
            //ToDo: get real service instance id
            service_instance_id = "id02";
            // Iterate through the usage arraylist to extract the userid and usage.
            // Multiple the usage with the rate of the resource and save it into an arraylist
            for(int j=0; j<usageListArr.size(); j++){
                usageArr = (ArrayList) usageListArr.get(j);
                for(int k=0; k<usageListArr.size(); k++){//resourceUsageStr.get(usage).size()
                    objArrNode = new ArrayList<Object>();
                    //userid = (String) usageArr.get(indexUserId);
                    usage = usageArr.get(indexUsage);
                    // Calculate the charge for a resource per user
                    charge = (Double.parseDouble(usage.toString())*rate);
                    objArrNode.add(enabledResourceList.get(i).toString());
                    objArrNode.add(userid);
                    objArrNode.add(usage);
                    objArrNode.add(charge);
                    objArr.add(objArrNode);
                }
            }
        }
        // Save the charge array into the database
        result = savePrice(objArr);
        logger.trace("END boolean generateCdr() throws IOException, JSONException");
        return result;
    }

    /**
     * Calculate the average rate for a time period
     *
     * Pseudo Code
     * 1. Get the array of rates for a resource
     * 2. Add the rates and get the average value
     * 3. Return the average rate.
     *
     * @param tsdbData Response from the DB for containing the list of rates
     * @return Double
     */
    private Double calculateRate(TSDBData tsdbData) {
        logger.trace("BEGIN Double calculateRate(TSDBData tsdbData)");
        int rateIndex;
        int rateDataPoints;
        Double rate = 0.0;
        ArrayList dataPointsArray;

        rateIndex = tsdbData.getColumns().indexOf("rate");
        rateDataPoints = tsdbData.getPoints().size();
        for (int i=0; i<tsdbData.getPoints().size();i++){
            dataPointsArray = tsdbData.getPoints().get(i);
            rate = rate + Double.parseDouble(dataPointsArray.get(rateIndex)+"");
        }
        logger.trace("DATA Double calculateRate(TSDBData tsdbData): rate="+rate);
        rate = rate/tsdbData.getPoints().size();
        logger.trace("END Double calculateRate(TSDBData tsdbData)");
        return rate;
    }

    /**
     * Save the price generated into the DB
     *
     * Pseudo Code
     * 1. Create the dataobj containing the details
     * 2. Save it into the db
     *
     * @param objArr Response from the DB for containing the list of rates
     * @return boolean
     */
    private boolean savePrice(ArrayList<ArrayList<Object>> objArr) {
        //TODO: create string array construction method with String... array
        //TODO: create generic method to create POJO object from objArr and map to JSON data
        logger.trace("BEGIN boolean savePrice(ArrayList<ArrayList<Object>> objArr)");
        boolean result = false;
        TSDBData pricingData = new TSDBData();
        ArrayList<String> strArr = new ArrayList<String>();
        InfluxDBClient dbClient = new InfluxDBClient();
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = null;

        strArr.add("resource");
        strArr.add("userid");
        strArr.add("usage");
        strArr.add("price");
        pricingData.setName("cdr6");//changed to cdr6 from cdr to avoid influxdb client problems till db is dumped
        pricingData.setColumns(strArr);
        pricingData.setPoints(objArr);
        //get tags and put them into pricingData
        logger.trace("DATA boolean savePrice(ArrayList<ArrayList<Object>> objArr): pricingData="+pricingData);

        try {
            jsonData = mapper.writeValueAsString(pricingData);
        } catch (JsonProcessingException e) {
            logger.error("EXCEPTION JSONPROCESSINGEXCEPTION boolean savePrice(ArrayList<ArrayList<Object>> objArr)");
            e.printStackTrace();
        }

        //System.out.println(jsonData);
        logger.trace("DATA boolean savePrice(ArrayList<ArrayList<Object>> objArr): jsonData="+jsonData);
        result = dbClient.saveData(jsonData);
        logger.trace("END boolean savePrice(ArrayList<ArrayList<Object>> objArr)");
        return result;
    }
}
