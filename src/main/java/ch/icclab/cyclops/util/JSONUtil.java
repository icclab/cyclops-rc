package ch.icclab.cyclops.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

/**
 * Created by Konstantin on 29.10.2015.
 */
public class JSONUtil {

    /**
     * Converts the response object into a JSON string
     *
     * @param pojoObj a response POJO with values set
     * @return a Representation object with JSON string
     */

    public Representation toJson(Object pojoObj) {
        ObjectMapper mapper = new ObjectMapper();
        String output;
        JsonRepresentation responseJson = null;

        try {
            output = mapper.writeValueAsString(pojoObj);
            responseJson = new JsonRepresentation(output);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return responseJson;
    }

}
