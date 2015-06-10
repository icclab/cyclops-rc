package ch.icclab.cyclops.model;

/**
 * Author: Srikanta
 * Created on: 19-May-15
 * Description: The class is responsible for accepting the response from the rate engine
 */
public class RateEngineResponse {
    private String resource;
    private double rate;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
