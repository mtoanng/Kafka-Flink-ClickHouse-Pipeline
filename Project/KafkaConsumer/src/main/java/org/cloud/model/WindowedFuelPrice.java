package org.cloud.model;

import java.io.Serializable;

/**
 * Kết quả tổng hợp (aggregation) trong một cửa sổ thời gian.
 * Được ghi vào bảng fuel_price_window_agg trong PostgreSQL.
 */
public class WindowedFuelPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    public String  windowStart;    // "2025-04-12T10:30:00"
    public String  windowEnd;      // "2025-04-12T10:31:00"
    public String  fuelType;       // WTI_CRUDE | BRENT_CRUDE | ...
    public String  location;       // New York (NYMEX)
    public String  region;
    public double  avgPrice;
    public double  minPrice;
    public double  maxPrice;
    public long    recordCount;

    public WindowedFuelPrice() {}

    public WindowedFuelPrice(String windowStart, String windowEnd,
                              String fuelType, String location, String region,
                              double avgPrice, double minPrice, double maxPrice, long recordCount) {
        this.windowStart = windowStart;
        this.windowEnd   = windowEnd;
        this.fuelType    = fuelType;
        this.location    = location;
        this.region      = region;
        this.avgPrice    = avgPrice;
        this.minPrice    = minPrice;
        this.maxPrice    = maxPrice;
        this.recordCount = recordCount;
    }

    @Override
    public String toString() {
        return String.format("[Window %s→%s] %s @ %s: avg=%.0f min=%.0f max=%.0f (n=%d)",
                windowStart, windowEnd, fuelType, location, avgPrice, minPrice, maxPrice, recordCount);
    }
}
