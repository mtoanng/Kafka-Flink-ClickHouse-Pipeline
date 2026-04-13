package org.cloud.model;

import java.io.Serializable;

/**
 * Cảnh báo biến động giá.
 * Được tạo khi giá thay đổi > ALERT_THRESHOLD_PERCENT so với giá gần nhất.
 * Được ghi vào bảng fuel_price_alerts trong PostgreSQL.
 */
public class PriceAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Ngưỡng cảnh báo: giá thay đổi > 3% */
    public static final double ALERT_THRESHOLD_PERCENT = 3.0;

    public String timestamp;
    public String fuelType;
    public String location;
    public String region;
    public double previousPrice;
    public double currentPrice;
    public double changePercent;
    public String alertType;    // PRICE_SPIKE | PRICE_DROP

    public PriceAlert() {}

    public PriceAlert(String timestamp, String fuelType, String location, String region,
                      double previousPrice, double currentPrice) {
        this.timestamp     = timestamp;
        this.fuelType      = fuelType;
        this.location      = location;
        this.region        = region;
        this.previousPrice = previousPrice;
        this.currentPrice  = currentPrice;
        this.changePercent = (currentPrice - previousPrice) / previousPrice * 100.0;
        this.alertType     = changePercent > 0 ? "PRICE_SPIKE" : "PRICE_DROP";
    }

    @Override
    public String toString() {
        return String.format("[ALERT %s] %s @ %s: %.2f%% (%s → %s)",
                alertType, fuelType, location, changePercent, previousPrice, currentPrice);
    }
}
