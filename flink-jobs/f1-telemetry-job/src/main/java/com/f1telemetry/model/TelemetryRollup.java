package com.f1telemetry.model;

import java.io.Serializable;

/**
 * 10-second windowed rollup per driver.
 *
 * Output of aggregation. Includes:
 * - avgSpeed, maxSpeed, avgThrottle: statistical aggregates
 * - hardBrakeCount: count of samples where brake >= 0.8 (conditional counting)
 * - sampleCount: total telemetry samples in window
 */
public class TelemetryRollup implements Serializable {
    private static final long serialVersionUID = 1L;

    public long windowStart;
    public int driverNumber;
    public double avgSpeed;
    public double maxSpeed;
    public double avgThrottle;
    public int hardBrakeCount;
    public int sampleCount;

    public TelemetryRollup() {}

    public TelemetryRollup(long windowStart, int driverNumber,
                           double avgSpeed, double maxSpeed, double avgThrottle,
                           int hardBrakeCount, int sampleCount) {
        this.windowStart = windowStart;
        this.driverNumber = driverNumber;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;
        this.avgThrottle = avgThrottle;
        this.hardBrakeCount = hardBrakeCount;
        this.sampleCount = sampleCount;
    }

    @Override
    public String toString() {
        return String.format("Rollup{t=%d, driver=%d, avg=%.1f, max=%.1f, brake=%d, n=%d}",
                windowStart, driverNumber, avgSpeed, maxSpeed, hardBrakeCount, sampleCount);
    }
}
