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

    public long window_start;
    public int driver_number;
    public double avg_speed;
    public double max_speed;
    public double avg_throttle;
    public long hard_brake_count;
    public long sample_count;

    public TelemetryRollup() {}

    public TelemetryRollup(long window_start, int driver_number,
                           double avg_speed, double max_speed, double avg_throttle,
                           long hard_brake_count, long sample_count) {
        this.window_start = window_start;
        this.driver_number = driver_number;
        this.avg_speed = avg_speed;
        this.max_speed = max_speed;
        this.avg_throttle = avg_throttle;
        this.hard_brake_count = hard_brake_count;
        this.sample_count = sample_count;
    }

    @Override
    public String toString() {
        return String.format("Rollup{t=%d, driver=%d, avg=%.1f, max=%.1f, brake=%d, n=%d}",
                window_start, driver_number, avg_speed, max_speed, hard_brake_count, sample_count);
    }
}
