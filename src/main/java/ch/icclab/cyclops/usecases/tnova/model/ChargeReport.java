package ch.icclab.cyclops.usecases.tnova.model;

import java.util.HashMap;

/**
 * @author Manu
 *         Created on 08.12.15.
 */
public class ChargeReport {
    private Double price;
    private HashMap<String, String> time;
    private String userid;

    public void setTime(String from, String to) {
        this.time = new HashMap<String, String>();
        this.time.put("from", from);
        this.time.put("to", to);
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
