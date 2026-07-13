package com.f1telemetry;

import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import org.apache.flink.api.common.functions.AggregateFunction;

/**
 * 10-second tumbling window aggregate per driver.
 *
 * Computes:
 *   avgSpeed       — mean speed in window
 *   maxSpeed       — peak speed in window
 *   avgThrottle    — mean throttle (non-null only)
 *   hardBrakeCount — samples where brake > 0 (conditional counting = the "transformation")
 *   sampleCount    — total samples in window
 */
public class SpeedRollupAggregator
        implements AggregateFunction<CarTelemetryEvent, SpeedRollupAggregator.Accumulator, TelemetryRollup> {

    public static class Accumulator {
        public long windowStart;
        public int driverNumber;
        public double speedSum;
        public double maxSpeed;
        public double throttleSum;
        public int throttleCount;
        public long hardBrakeCount;
        public long sampleCount;
    }

    @Override
    public Accumulator createAccumulator() {
        Accumulator acc = new Accumulator();
        acc.windowStart = Long.MAX_VALUE;
        acc.maxSpeed = -Double.MAX_VALUE; // not MIN_VALUE (that's a small negative)
        return acc;
    }

    @Override
    public Accumulator add(CarTelemetryEvent event, Accumulator acc) {
        if (acc.windowStart == Long.MAX_VALUE) {
            // Window start = event_time rounded down to 10s boundary
            acc.windowStart = event.getEventTime() - (event.getEventTime() % 10_000L);
            acc.driverNumber = event.getDriverNumber();
        }

        acc.speedSum += event.getSpeed();
        if (event.getSpeed() > acc.maxSpeed) acc.maxSpeed = event.getSpeed();
        acc.sampleCount++;

        if (event.getThrottle() != null) {
            acc.throttleSum += event.getThrottle();
            acc.throttleCount++;
        }

        // Conditional count: brake > 0 means driver is braking
        if (event.getBrake() != null && event.getBrake() > 0) {
            acc.hardBrakeCount++;
        }

        return acc;
    }

    @Override
    public TelemetryRollup getResult(Accumulator acc) {
        return new TelemetryRollup(
                acc.windowStart,
                acc.driverNumber,
                acc.sampleCount > 0 ? acc.speedSum / acc.sampleCount : 0,
                acc.maxSpeed == -Double.MAX_VALUE ? 0 : acc.maxSpeed,
                acc.throttleCount > 0 ? acc.throttleSum / acc.throttleCount : 0,
                acc.hardBrakeCount,
                acc.sampleCount
        );
    }

    @Override
    public Accumulator merge(Accumulator a, Accumulator b) {
        if (b.windowStart < a.windowStart) a.windowStart = b.windowStart;
        a.speedSum += b.speedSum;
        a.maxSpeed = Math.max(a.maxSpeed, b.maxSpeed);
        a.throttleSum += b.throttleSum;
        a.throttleCount += b.throttleCount;
        a.hardBrakeCount += b.hardBrakeCount;
        a.sampleCount += b.sampleCount;
        return a;
    }
}
