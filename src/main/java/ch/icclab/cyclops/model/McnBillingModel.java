package ch.icclab.cyclops.model;

/**
 * @author Manu
 * Created by root on 06.11.15.
 */
public class McnBillingModel extends BillingModel {
    //used for mapping
    private double price = 0.0;
    private String rate_policy;
    private String resource;
    private String date;

    @Override
    public double computeCost(double time) {
        return this.price*time;
    }

    //getters and setters

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRate_policy() {
        return rate_policy;
    }

    public void setRate_policy(String rate_policy) {
        this.rate_policy = rate_policy;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
