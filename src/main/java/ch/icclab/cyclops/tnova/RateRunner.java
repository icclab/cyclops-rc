package ch.icclab.cyclops.tnova;

import ch.icclab.cyclops.resource.client.InfluxDBClient;
import ch.icclab.cyclops.util.Load;
import org.restlet.resource.ClientResource;

/**
 * @author Manu
 *         Created by root on 11.11.15.
 */
public class RateRunner implements Runnable {

    private InfluxDBClient dbClient;

    public RateRunner(){
        this.dbClient = new InfluxDBClient();
    }

    public void run() {
        regenerateRate();
    }

    private void regenerateRate() {
        String url = Load.configuration.get("localhost");
        ClientResource clientResource = new ClientResource(url+"/generate/rate");
        clientResource.get();
    }
}
