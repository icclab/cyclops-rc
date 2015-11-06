package ch.icclab.cyclops.tnova;

import ch.icclab.cyclops.model.BillingModel;
import ch.icclab.cyclops.model.UDRResponse;
import ch.icclab.cyclops.resource.client.InfluxDBClient;
import ch.icclab.cyclops.util.Load;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import java.io.IOException;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Pull data from UDR
 */
public class TNovaPuller {
    final static Logger logger = LogManager.getLogger(TNovaPuller.class.getName());

    // endpoint URL for UDR
    String udrEndpoint;

    // connection to DB for getting last date
    InfluxDBClient dbClient;

    /**
     * Constructor that will populate class variables
     */
    public TNovaPuller() {
        this.udrEndpoint = new Load().getUDREndpointURL();
        this.dbClient = new InfluxDBClient();
    }

    public UDRResponse retrieveUsageResponseFromUDR() {
        // construct usage retrieval url
        String url = constructUsageQuery(dbClient.getLastPull());

        try {
            // retrieve response
            String response = pullData(url);

            // if it's empty return null
            if (response.isEmpty()) {
                return null;
            } else {
                // return parsed object
                return parseResponse(response);
            }

        } catch (Exception e) {
            logger.error("Couldn't retrieve response from UDR: " + e.getMessage());
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Parse provided response and return UDRResponse POJO object
     * @param response
     * @return object or null
     */
    private UDRResponse parseResponse(String response) {
        Gson gson = new Gson();

        return gson.fromJson(response, UDRResponse.class);
    }

    /**
     * Pull data from provided URL
     * @param url
     * @return output string or empty string
     */
    private String pullData(String url) throws IOException {
        Client client = new Client(Protocol.HTTP);
        ClientResource cr = new ClientResource(url);
        Request req = cr.getRequest();

        // now header
        Series<Header> headerValue = new Series<Header>(Header.class);
        req.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headerValue);
        headerValue.add("Accept", "application/json");
        headerValue.add("Content-Type", "application/json");

        // fire it up
        cr.get(MediaType.APPLICATION_JSON);
        Representation output = cr.getResponseEntity();

        // return null or received text
        return (output == null) ? "" : output.getText();
    }

    /**
     * Create T-Nova Usage query - with date specified
     * @param fromDate
     * @return
     */
    private String constructUsageQuery(String fromDate) {
        if (fromDate.isEmpty()) {
            return udrEndpoint + "/mcn/usage";
        } else {
            return udrEndpoint + "/mcn/usage?from=" + fromDate;
        }
    }
}
