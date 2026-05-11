package org.cloud.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Mô hình dữ liệu giá dầu THẾ GIỚI — Flink side.
 *
 * Dùng @JsonProperty của Flink's shaded Jackson.
 *
 * JSON từ Kafka:
 * {
 *   "timestamp":  "2025-04-12T10:30:00",
 *   "fuel_type":  "WTI_CRUDE",
 *   "price":      78.45,
 *   "price_unit": "USD/barrel",
 *   "location":   "New York (NYMEX)",
 *   "region":     "North America",
 *   "source":     "mock-generator"
 * }
 *
 * Fuel types:
 *   WTI_CRUDE        → USD/barrel  (NYMEX, New York)
 *   BRENT_CRUDE      → USD/barrel  (ICE,   London)
 *   GASOLINE_FUTURES → USD/gallon  (NYMEX, New York)
 *   DIESEL           → USD/gallon  (US market)
 *   NATURAL_GAS      → USD/MMBtu   (Henry Hub)
 */
public class FuelPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("timestamp")
    public String timestamp;           // "2025-04-12T10:30:00"

    @JsonProperty("fuel_type")
    public String fuelType;            // WTI_CRUDE | BRENT_CRUDE | GASOLINE_FUTURES | DIESEL | NATURAL_GAS

    @JsonProperty("price")
    public double price;               // giá theo đơn vị priceUnit

    @JsonProperty("price_unit")
    public String priceUnit;           // USD/barrel | USD/gallon | USD/MMBtu

    @JsonProperty("location")
    public String location;            // "New York (NYMEX)" | "London (ICE)" | ...

    @JsonProperty("region")
    public String region;              // North America | Europe | Asia Pacific | Middle East

    @JsonProperty("source")
    public String source;              // "mock-generator" | "alpha-vantage"

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Key dùng cho keyBy: mỗi (fuel_type, region) là 1 stream riêng */
    public String getCompositeKey() {
        return fuelType + "::" + region;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s @ %s = %.4f %s",
                timestamp, fuelType, location, price, priceUnit);
    }
}