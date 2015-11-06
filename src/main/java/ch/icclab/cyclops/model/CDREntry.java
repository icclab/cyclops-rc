package ch.icclab.cyclops.model;

import ch.icclab.cyclops.util.DateTimeUtil;
import org.influxdb.dto.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Holds CDR Entry object after calculation of all required fields
 */
public class CDREntry {
    private String name = "mcn_cdr";
    private String time;
    private String productId;
    private String resource; // basically instanceId
    private String userId;   // basically clientId
    private double price;
    private String priceUnit;
    private String period;
    private double periodCost;
    private double setupCost;
    private String usage;

    /**
     * Construct and populate CDREntry object
     * @param udr object
     * @param model for billing
     * @param clientId as a string
     */
    public CDREntry(UDREntry udr, McnBillingModel model, String clientId) {
        // basic header
        this.time = udr.getTime();
        //this.productId = udr.getProductId();
        this.resource = udr.getInstanceId();
        this.userId = clientId;

        // compute price and save usage
        this.price = model.computeCost(Double.parseDouble(udr.getUsedSeconds())); // TODO maybe change to long?
        this.usage = udr.getUsedSeconds();

//        // cdr records
//        this.priceUnit = model.getPriceUnit();
//        this.period = model.getPeriod();
//        this.periodCost = model.getPeriodCost();
//        this.setupCost = (udr.shouldAddSetupCost())? model.getSetupCost() : 0;
    }

    /**
     * Create an InfluxDB Point that can be saved into InfluxDB database
     * @return
     */
    public Point toDBPoint() {

        Map tags = getTags();
        removeNullValues(tags);

        Map fields = getFields();
        removeNullValues(fields);

        // now return constructed point
        return Point.measurement(name)
                .time(DateTimeUtil.getMillisForTime(time), TimeUnit.MILLISECONDS)
                .tag(tags)
                .fields(fields)
                .build();
    }

    /**
     * This method returns default tags
     * @return
     */
    private Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        // now add default tags
        map.put("productId", productId);
        map.put("resource", resource);
        map.put("userId", userId);

        return map;
    }

    /**
     * This method returns default fields
     * @return
     */
    private Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        // now add default fields
        map.put("price", price);
        map.put("priceUnit", priceUnit);
        map.put("period", period);
        map.put("periodCost", periodCost);
        map.put("setupCost", setupCost);
        map.put("usage", usage);

        return map;
    }

    /**
     * Make sure we are not having any null values
     * @param map original container that has to be changed
     */
    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }
}
