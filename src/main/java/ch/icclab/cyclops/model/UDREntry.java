package ch.icclab.cyclops.model;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds UDR Entry, as a row line from UDR Record
 */
public class UDREntry {
    private String time;
    private String instanceId;
    private String productId;
    private String productType;
    private String flagSetupCost;
    private String to;
    private String usage;

    /**
     * Will return boolean based on flagSetupCost
     * @return
     */
    public Boolean shouldAddSetupCost() {
        return Boolean.valueOf(flagSetupCost);
    }

    //=========== Setters and Getters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String type) {
        this.productType = type;
    }

    public String getFlagSetupCost() {
        return flagSetupCost;
    }

    public void setFlagSetupCost(String flagSetupCost) {
        this.flagSetupCost = flagSetupCost;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }
}
