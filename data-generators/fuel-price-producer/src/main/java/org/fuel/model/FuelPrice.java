package org.fuel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mô hình dữ liệu giá nhiên liệu THẾ GIỚI.
 * Được dùng ở cả Producer (serialize → JSON) và Flink Consumer (deserialize ← JSON).
 *
 * JSON mẫu:
 * {
 *   "timestamp":  "2025-04-12T10:30:00",
 *   "fuel_type":  "WTI_CRUDE",
 *   "price":      78.45,
 *   "price_unit": "USD/barrel",
 *   "location":   "New York",
 *   "region":     "North America",
 *   "source":     "mock-generator"
 * }
 *
 * Fuel types:
 *   WTI_CRUDE   → USD/barrel  (NYMEX, New York)
 *   BRENT_CRUDE → USD/barrel  (ICE,   London)
 *   GASOLINE    → USD/gallon  (US market)
 *   DIESEL      → USD/gallon  (US market)
 *   NATURAL_GAS → USD/MMBtu   (Henry Hub)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuelPrice {

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String timestamp;

    /** WTI_CRUDE | BRENT_CRUDE | GASOLINE | DIESEL | NATURAL_GAS */
    @JsonProperty("fuel_type")
    private String fuelType;

    /** Giá theo đơn vị tự nhiên của từng loại nhiên liệu */
    @JsonProperty("price")
    private double price;

    /** USD/barrel | USD/gallon | USD/MMBtu */
    @JsonProperty("price_unit")
    private String priceUnit;

    /** Tên sàn giao dịch / thị trường */
    @JsonProperty("location")
    private String location;

    /** North America | Europe | Middle East | Asia Pacific */
    @JsonProperty("region")
    private String region;

    /** Nguồn dữ liệu: mock-generator | alpha-vantage | eia-api */
    @JsonProperty("source")
    private String source;
}
