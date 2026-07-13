package com.f1telemetry;

import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.DriverSpeed;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Tracks maximum speed per driver using keyed state.
 * Emits DriverSpeed record whenever a new max speed is detected.
 * 
 * This is a stateful operator that maintains per-driver max speed
 * and only outputs when the max increases (change detection).
 */
public class MaxSpeedTracker extends KeyedProcessFunction<Integer, CarTelemetryEvent, DriverSpeed> {

    private transient ValueState<Double> maxSpeedState;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ValueStateDescriptor<Double> descriptor = 
            new ValueStateDescriptor<>("max-speed", Double.class, 0.0);
        maxSpeedState = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void processElement(
            CarTelemetryEvent event,
            Context ctx,
            Collector<DriverSpeed> out) throws Exception {

        Double currentMax = maxSpeedState.value();
        double eventSpeed = event.getSpeed();

        // Only emit when new max is detected (reduces Redis writes)
        if (eventSpeed > currentMax) {
            maxSpeedState.update(eventSpeed);
            out.collect(new DriverSpeed(
                event.getDriverNumber(),
                eventSpeed,
                event.getEventTime()
            ));
        }
    }
}
