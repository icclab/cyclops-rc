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

import ch.icclab.cyclops.usecases.tnova.resource.RevenueSharingResource;
import ch.icclab.cyclops.util.DateTimeUtil;
import org.influxdb.dto.Point;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Manu
 *         Created on 04.12.15.
 */
public class RevenueSharingEntry {
    /*
      "SP": "sp1",
      "instance": "efasdfÇ",
      "vnf": "1151",
      "earnings": 1431,

      "SLA_penalties": 4,
      "discounts": 21,
      "unit": "EUR",
      "total": 234

          //needed for the pojo class so we can have the structure.
     */
    private String name = "tnova_revenue_sharing";
    private String SProvider;
    private String instanceId;
    private double price;
    private String priceUnit;

    private String time;
    private String VNFProvider;

    public RevenueSharingEntry() {
    }

    public RevenueSharingEntry(TnovaCDREntry cdr, String vnfInstanceId, TnovaBillingModel billingModel) {
        this.time = cdr.getTime();
        this.instanceId = vnfInstanceId;
        this.VNFProvider = getVnfProvider(vnfInstanceId);
        this.SProvider = cdr.getProviderId();//CDR's provider is the Service Provider
        this.priceUnit = cdr.getPriceUnit();
        this.price = computeCost(billingModel, cdr);
    }

    private String getVnfProvider(String vnfInstanceId) {
        RevenueSharingResource revenueSharingResource = new RevenueSharingResource();
        return revenueSharingResource.getProviderId(vnfInstanceId);
    }

    private double computeCost(TnovaBillingModel billingModel, TnovaCDREntry cdr) {
        if (billingModel.getBillingModel().equals("%")) {
            return (billingModel.getPeriodCost() / 100) * cdr.getPrice();
        } else {
            return billingModel.getPeriodCost() * cdr.getPrice();
        }
    }

    /**
     * Create an InfluxDB Point that can be saved into InfluxDB database
     *
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
     *
     * @return
     */
    protected Map<String, String> getTags() {
        Map<String, String> map = new HashMap<String, String>();

        // now add default tags
        map.put("instanceId", instanceId);
        map.put("VNFProvider", VNFProvider);
        map.put("SProvider", SProvider);

        return map;
    }

    /**
     * This method returns default fields
     *
     * @return
     */
    protected Map<String, Object> getFields() {
        Map<String, Object> map = new HashMap<String, Object>();

        // now add default fields
        map.put("price", price);
        map.put("priceUnit", priceUnit);

        return map;
    }

    private void removeNullValues(Map<Object, Object> map) {
        map.values().removeAll(Collections.singleton(null));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSProvider(String SProvider) {
        this.SProvider = SProvider;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setVNFProvider(String VNFProvider) {
        this.VNFProvider = VNFProvider;
    }
}
