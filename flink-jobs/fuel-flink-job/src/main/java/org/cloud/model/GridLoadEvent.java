package org.cloud.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Pillar 3 — Sự kiện phụ tải lưới điện theo region.
 *
 * Khớp JSON producer {@code GridLoadGenerator}:
 * {
 *   "region_code":  "VN_NORTH",
 *   "load_mw":      9420.5,
 *   "capacity_mw":  12000.0,
 *   "is_peak_hour": true,
 *   "event_time":   "2026-05-12T19:05:00"
 * }
 *
 * Schema target: {@code grid_load_raw}. Cột {@code load_pct} là generated, Postgres tự tính.
 */
public class GridLoadEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("region_code")
    public String regionCode;

    @JsonProperty("load_mw")
    public double loadMw;

    @JsonProperty("capacity_mw")
    public double capacityMw;

    @JsonProperty("is_peak_hour")
    public boolean peakHour;

    @JsonProperty("event_time")
    public String eventTime;

    /** Tính tỷ lệ tải (%) trên Flink-side để rule-engine so sánh nhanh. */
    public double getLoadPct() {
        return capacityMw > 0 ? (loadMw / capacityMw) * 100.0 : 0.0;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s load=%.2f/%.2f MW (%.1f%%) peak=%s",
                eventTime, regionCode, loadMw, capacityMw, getLoadPct(), peakHour);
    }
}
