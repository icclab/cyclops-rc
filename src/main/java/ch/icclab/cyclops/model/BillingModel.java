package ch.icclab.cyclops.model;

import org.joda.time.DateTime;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds Billing Model JSON Response from Accounting module (used for mapping and calculations)
 */
public abstract class BillingModel {

    /**
     * Compute cost for the billing model and provided time
     * @param time has to be of double data primitive
     */
    public abstract Double computeCost(double time);
}
