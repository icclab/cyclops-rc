package ch.icclab.cyclops.resource.client;

import ch.icclab.cyclops.model.RateEngineResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import ch.icclab.cyclops.util.Load;

import java.io.IOException;

/**
 * Author: Srikanta
 * Created on: 19-May-15
 * Description: Client class for connecting to the rule engine
 *
 */
public class RuleEngineClient extends ClientResource {

    /**
     * Gets the rate for a particular resource
     *
     * Pseudo Code
     * 1. Load the URL of the rate engine
     * 2. Query the rule engine to get the rate for a resource
     * 3. Convert the resonse into a JSON object
     *
     * @param resourceName A string containing the name of the resource
     * @return RateEngineResponse
     */
    public RateEngineResponse getRate(String resourceName){
        JsonRepresentation jsonRepresentation = null;
        RateEngineResponse responseObj = null;
        ObjectMapper mapper = new ObjectMapper();
        String url = Load.configuration.get("RuleEngineUrl");

        Client client = new Client(Protocol.HTTP);
        ClientResource resource = new ClientResource(url+"rate/"+resourceName);
        resource.get(MediaType.APPLICATION_JSON);
        Representation output = resource.getResponseEntity();
        try {
            System.out.println(output.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jsonRepresentation = new JsonRepresentation(output.getText());
            System.out.println(jsonRepresentation.getText());
            double rate = (Double) jsonRepresentation.getJsonObject().get("rate");
            System.out.println(rate);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            //JSONObject jsonStr = new JSONObject(output);
            responseObj = mapper.readValue(jsonRepresentation.getJsonObject().toString(),RateEngineResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return responseObj;
    }
}
