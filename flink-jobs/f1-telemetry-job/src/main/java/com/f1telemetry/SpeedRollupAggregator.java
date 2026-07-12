package com.f1telemetry;

import org.apache.flink.api.common.functions.AggregateFunction;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;

/**
 * Aggregates 10s window of telemetry events per driver.
 * Computes: avg_speed, max_speed, avg_throttle
 */
public class SpeedRollupAggregator implements 
        AggregateFunction<CarTelemetryEvent, SpeedRollupAggregator.Accumulator, TelemetryRollup> {
    
    /**
     * Accumulator: holds running sum/count/max during window
     */
    public static class Accumulator {
        long windowStart;
        int driverNumber;
        double speedSum;
        double maxSpeed;
        double throttleSum;
        int count;
        int throttleCount;  // Throttle can be null, count separately
        
        public Accumulator() {
            this.maxSpeed = Double.MIN_VALUE;
        }
    }
    
    @Override
    public Accumulator createAccumulator() {
        return new Accumulator();
    }
    
    @Override
    public Accumulator add(CarTelemetryEvent event, Accumulator acc) {
        // Initialize window start and driver on first event
        if (acc.count == 0) {
            acc.windowStart = event.getEventTime();
            acc.driverNumber = event.getDriverNumber();
        }
        
        // Accumulate speed
        acc.speedSum += event.getSpeed();
        if (event.getSpeed() > acc.maxSpeed) {
            acc.maxSpeed = event.getSpeed();
        }
        acc.count++;
        
        // Accumulate throttle (nullable)
        if (event.getThrottle() != null) {
            acc.throttleSum += event.getThrottle();
            acc.throttleCount++;
        }
        
        return acc;
    }
    
    @Override
    public TelemetryRollup getResult(Accumulator acc) {
        if (acc.count == 0) {
            return new TelemetryRollup(0, 0, 0.0, 0.0, 0.0);
        }
        
        double avgSpeed = acc.speedSum / acc.count;
        double avgThrottle = acc.throttleCount > 0 
                ? acc.throttleSum / acc.throttleCount 
                : 0.0;
        
        return new TelemetryRollup(
                acc.windowStart,
                acc.driverNumber,
                avgSpeed,
                acc.maxSpeed,
                avgThrottle
        );
    }
    
    @Override
    public Accumulator merge(Accumulator a, Accumulator b) {
        // Merge two accumulators (for session windows or combining partial results)
        a.speedSum += b.speedSum;
        a.maxSpeed = Math.max(a.maxSpeed, b.maxSpeed);
        a.throttleSum += b.throttleSum;
        a.count += b.count;
        a.throttleCount += b.throttleCount;
        return a;
    }
}
