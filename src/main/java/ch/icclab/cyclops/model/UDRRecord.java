package ch.icclab.cyclops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds UDR Record object (parsed from UDRResponse)
 */
public class UDRRecord {

    private String name;

    private List<String> columns;

    private List<List<String>> points;

    private Tag tags;

    public class Tag {
        private String clientId;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }

    /**
     * Parse out details from columns and based on header create objects
     * @return UDREntry object
     */
    public List<UDREntry> getListOfEntries () {
        List<UDREntry> list = new ArrayList<UDREntry>();

        // get indexes
        Integer time = columns.indexOf("time");
        Integer instanceId = columns.indexOf("instanceId");
        //Integer productId = columns.indexOf("productId");
        Integer type = columns.indexOf("productType");
        //Integer flagSetupCost = columns.indexOf("flagSetupCost");
        Integer to = columns.indexOf("to");
        Integer usage = columns.indexOf("usage");

        // iterate over all rows and fill the objects
        for (List<String> row : points) {
            UDREntry entry = new UDREntry();

            // fill everything
            entry.setTime(row.get(time));
            entry.setInstanceId(row.get(instanceId));
            //entry.setProductId(row.get(productId));
            entry.setProductType(row.get(type));
            //entry.setFlagSetupCost(row.get(flagSetupCost));
            entry.setTo(row.get(to));
            entry.setUsage(row.get(usage));

            // now add it to the list
            list.add(entry);
        }

        return list;
    }

    /**
     * Will return clientId of the records
     * @return
     */
    public String getClientId() {
        return tags.getClientId();
    }

    //============ Setters and Getters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getPoints() {
        return points;
    }

    public void setPoints(List<List<String>> points) {
        this.points = points;
    }

    public Tag getTags() {
        return tags;
    }

    public void setTags(Tag tags) {
        this.tags = tags;
    }
}
