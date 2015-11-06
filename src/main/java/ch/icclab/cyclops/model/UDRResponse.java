package ch.icclab.cyclops.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Martin Skoviera
 * Created on: 02-Nov-15
 * Description: Holds JSON Response from UDR Micro-service (used for GSON mapping)
 */
public class UDRResponse {

    private List<UDRRecord> udr_records;

    public List<UDRRecord> getUdr_records() {
        return udr_records;
    }

    public void setUdr_records(List<UDRRecord> udr_records) {
        this.udr_records = udr_records;
    }
}
