/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

package ch.icclab.cyclops.usecases.tnova.model;

import ch.icclab.cyclops.model.BillingModel;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds Billing Model JSON Response from Accounting module (used for mapping and calculations)
 */
public class TnovaBillingModel extends BillingModel {
    // used for mapping
    private String startDate;
    private String lastBillDate;
    private String billingModel;
    private String period;
    private String priceUnit;
    private double periodCost;
    private double setupCost;

    /**
     * Compute cost for the billing model and provided time
     * @param time has to be of double data primitive
     */
    @Override
    public Double computeCost(double time) {
        // get the time period and policy type
        int timePeriod = Integer.parseInt(period.substring(1, period.length()-1));
        char periodPolicy = period.charAt(period.length()-1);

        double finalPrice = 0.0;

        // compute the final price
        switch (periodPolicy) {
            case 'D':
                final int secondsPerDay = 86400;
                finalPrice = (time / (secondsPerDay * timePeriod)) * periodCost;
                break;
            case 'W':
                final int secondsPerWeek = 604800;
                finalPrice = (time / (secondsPerWeek * timePeriod)) * periodCost;
                break;
            case 'M':
                final int secondsPerMonth = secondsInMonth(2015, 10); // TODO do this based on real date
                finalPrice = (time / (secondsPerMonth * timePeriod)) * periodCost;
                break;
            case 'Y':
                final int secondsPerYear = 31536000;
                finalPrice = (time / (secondsPerYear * timePeriod)) * periodCost;
                break;
        }

        return finalPrice;
    }

    /**
     * Just a quick method to get number of days for the month
     * @param year
     * @param month
     * @return
     */
    private static int secondsInMonth(int year, int month) {
        DateTime dateTime = new DateTime(year, month, 14, 12, 0, 0, 000);
        Integer days = dateTime.dayOfMonth().getMaximumValue();
        return days * 24 * 60 * 60;
    }

    //====== Setters and Getters
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getLastBillDate() {
        return lastBillDate;
    }

    public void setLastBillDate(String lastBillDate) {
        this.lastBillDate = lastBillDate;
    }

    public String getBillingModel() {
        return billingModel;
    }

    public void setBillingModel(String billingModel) {
        this.billingModel = billingModel;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public double getPeriodCost() {
        return periodCost;
    }

    public void setPeriodCost(double periodCost) {
        this.periodCost = periodCost;
    }

    public double getSetupCost() {
        return setupCost;
    }

    public void setSetupCost(double setupCost) {
        this.setupCost = setupCost;
    }
}
