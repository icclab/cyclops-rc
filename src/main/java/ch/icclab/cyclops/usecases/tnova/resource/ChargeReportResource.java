//package ch.icclab.cyclops.usecases.tnova.resource;
//
//import ch.icclab.cyclops.load.Loader;
//import ch.icclab.cyclops.model.ChargeResponse;
//import ch.icclab.cyclops.usecases.tnova.model.ChargeReport;
//import ch.icclab.cyclops.usecases.tnova.model.RevenueSharingList;
//import ch.icclab.cyclops.util.APICallCounter;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.restlet.ext.json.JsonRepresentation;
//import org.restlet.representation.Representation;
//import org.restlet.resource.ClientResource;
//import org.restlet.resource.Get;
//import org.restlet.resource.ServerResource;
//
//import java.io.IOException;
//import java.util.HashMap;
//
///**
// * @author Manu
// *         Created on 08.12.15.
// */
//public class ChargeReportResource extends ServerResource{
//    final static Logger logger = LogManager.getLogger(ChargeReportResource.class.getName());
//
//    // who am I?
//    private String endpoint = "/charge/report";
//
//    // used as counter
//    private APICallCounter counter = APICallCounter.getInstance();
//
//    /**
//     * Queries the database to get the charge data records for a given time period
//     *
//     * Pseudo Code
//     * 1. Get the userid , from and to details from the API query parameters
//     * 2. Query the database to get the cdr
//     * 3. Construct the response and return the json string
//     *
//     * @return Representation
//     */
//    @Get
//    public String getChargeRecords(){
//
//        counter.increment(endpoint);
//
//        Representation response;
//        ObjectMapper objectMapper = new ObjectMapper();
//        String jsonStr;
//        Double sum = 0.0;
//
//
//        String userid = getQueryValue("userid");
//        String fromDate = normalizeDateAndTime(getQueryValue("from"));
//        String toDate = normalizeDateAndTime(getQueryValue("to"));
//
//        String urlParams = "/charge?userid="+userid+"&from="+fromDate+"&to="+toDate;
//
//        ClientResource clientResource = new ClientResource(Loader.getSettings().getCyclopsSettings().getRcServiceUrl()+ urlParams);
//
//        response = clientResource.get();
//        try {
//            jsonStr = response.getText();
//            Gson gson = new Gson();
//            RevenueSharingList revenueSharingList = gson.fromJson(jsonStr, RevenueSharingList.class);
//            sum = revenueSharingList.getAggregation();
//        } catch (IOException e) {
//            logger.debug("Error while mapping the Charge Report List object: "+ e.getMessage());
//        }
//
//        ChargeReport report = new ChargeReport();
//        report.setUserid(userid);
//        report.setPrice(sum);
//        report.setTime(fromDate, toDate);
//
//
//        Gson gson = new Gson();
//        String result = gson.toJson(report);
//        return result;
//    }
//
//    /**
//     *  Construct the JSON response consisting of the meter and the usage values
//     *
//     *  Pseudo Code
//     *  1. Create the HasMap consisting of time range
//     *  2. Create the response POJO
//     *  3. Convert the POJO to JSON
//     *  4. Return the JSON string
//     *
//     * @param rateArr An arraylist consisting of metername and corresponding usage
//     * @param fromDate DateTime from usage data needs to be calculated
//     * @param toDate DateTime upto which the usage data needs to be calculated
//     * @return responseJson The response object in the JSON format
//     */
//    public Representation constructResponse(HashMap rateArr, String userid, String fromDate, String toDate){
//
//        String jsonStr;
//        JsonRepresentation responseJson = null;
//
//        ChargeResponse responseObj = new ChargeResponse();
//        HashMap time = new HashMap();
//        ObjectMapper mapper = new ObjectMapper();
//
//        time.put("from",fromDate);
//        time.put("to",toDate);
//
//        //Build the response POJO
//        responseObj.setUserid(userid);
//        responseObj.setTime(time);
//        responseObj.setCharge(rateArr);
//
//        //Convert the POJO to a JSON string
//        try {
//            jsonStr = mapper.writeValueAsString(responseObj);
//            responseJson = new JsonRepresentation(jsonStr);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//
//        return responseJson;
//    }
//
//    /**
//     * Remove ' character and replace T with a space
//     * @param time
//     * @return
//     */
//    private String normalizeDateAndTime(String time) {
//        String first = time.replace("'", "");
//        return first.replace("T", " ");
//    }
//}
