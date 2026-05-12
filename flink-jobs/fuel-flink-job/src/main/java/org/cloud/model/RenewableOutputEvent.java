package org.cloud.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Pillar 4 — Sản lượng năng lượng tái tạo (SOLAR/WIND/HYDRO) theo region.
 *
 * Khớp JSON producer {@code RenewableGenerator}:
 * {
 *   "region_code":  "VN_SOUTH",
 *   "source_type":  "SOLAR",
 *   "output_mw":    1820.55,
 *   "capacity_mw":  2500.0,
 *   "event_time":   "2026-05-12T12:00:00"
 * }
 *
 * Schema: {@code renewable_output_raw}. Cột generated {@code utilization_pct}.
 */
public class RenewableOutputEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("region_code")
    public String regionCode;

    @JsonProperty("source_type")
    public String sourceType;    // SOLAR | WIND | HYDRO

    @JsonProperty("output_mw")
    public double outputMw;

    @JsonProperty("capacity_mw")
    public double capacityMw;

    @JsonProperty("event_time")
    public String eventTime;

    @Override
    public String toString() {
        return String.format("[%s] %s/%s output=%.2f MW (%.1f%% cap)",
                eventTime, regionCode, sourceType, outputMw,
                capacityMw > 0 ? (outputMw / capacityMw) * 100.0 : 0.0);
    }
}
