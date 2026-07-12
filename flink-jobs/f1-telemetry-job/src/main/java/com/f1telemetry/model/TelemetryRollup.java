package com.f1telemetry.model;

import java.io.Serializable;

/**
 * POJO for 10s rollup aggregation result
 */
public class TelemetryRollup implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public long windowStart;
    public int driverNumber;
    public double avgSpeed;
    public double maxSpeed;
    public double avgThrottle;
    
    public TelemetryRollup() {}
    
    public TelemetryRollup(long windowStart, int driverNumber, 
                           double avgSpeed, double maxSpeed, double avgThrottle) {
        this.windowStart = windowStart;
        this.driverNumber = driverNumber;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;
        this.avgThrottle = avgThrottle;
    }
    
    @Override
    public String toString() {
        return String.format("TelemetryRollup{window=%d, driver=%d, avgSpeed=%.1f, maxSpeed=%.1f, avgThrottle=%.1f}",
                windowStart, driverNumber, avgSpeed, maxSpeed, avgThrottle);
    }
}
