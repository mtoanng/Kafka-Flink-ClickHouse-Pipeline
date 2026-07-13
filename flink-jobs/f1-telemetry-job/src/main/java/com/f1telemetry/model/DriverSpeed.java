package com.f1telemetry.model;

import java.io.Serializable;

/**
 * Driver speed record for Redis leaderboard.
 * Contains driver number and their current max speed.
 */
public class DriverSpeed implements Serializable {
    private static final long serialVersionUID = 1L;

    public int driverNumber;
    public double maxSpeed;
    public long timestamp;

    public DriverSpeed() {}

    public DriverSpeed(int driverNumber, double maxSpeed, long timestamp) {
        this.driverNumber = driverNumber;
        this.maxSpeed = maxSpeed;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("DriverSpeed{driver=%d, speed=%.1f km/h, time=%d}",
                driverNumber, maxSpeed, timestamp);
    }
}
