package org.cloud.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Pillar 4 — Sự kiện phát thải CO2 từ phát điện trong 1 window.
 *
 * Khớp JSON producer {@code RenewableGenerator}:
 * {
 *   "region_code": "VN_NORTH",
 *   "co2_kg":      15234.5,
 *   "energy_mwh":  225.0,
 *   "event_time":  "2026-05-12T19:30:00"
 * }
 *
 * Schema: {@code emission_raw}. Cột generated {@code intensity_kg_per_mwh = co2_kg / energy_mwh}.
 */
public class EmissionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("region_code")
    public String regionCode;

    @JsonProperty("co2_kg")
    public double co2Kg;

    @JsonProperty("energy_mwh")
    public double energyMwh;

    @JsonProperty("event_time")
    public String eventTime;

    /** Tính intensity trên Flink-side để dùng cho rule-engine (EMISSION_INTENSITY). */
    public double getIntensityKgPerMwh() {
        return energyMwh > 0 ? co2Kg / energyMwh : 0.0;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s co2=%.2f kg / %.2f MWh = %.2f kg/MWh",
                eventTime, regionCode, co2Kg, energyMwh, getIntensityKgPerMwh());
    }
}
