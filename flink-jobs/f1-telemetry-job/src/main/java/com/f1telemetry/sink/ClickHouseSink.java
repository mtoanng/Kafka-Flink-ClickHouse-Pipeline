package com.f1telemetry.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * ClickHouse JDBC Sinks (2 implementations)
 * - RawTelemetrySink: writes all events to raw_telemetry
 * - RollupSink: writes 10s aggregates to rollup_10s
 */
public class ClickHouseSink {
    
    /**
     * Sink for raw telemetry events
     */
    public static class RawTelemetrySink extends RichSinkFunction<CarTelemetryEvent> {
        private static final Logger LOG = LoggerFactory.getLogger(RawTelemetrySink.class);
        
        private final String jdbcUrl;
        private transient Connection connection;
        private transient PreparedStatement stmt;
        
        public RawTelemetrySink(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }
        
        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            connection = DriverManager.getConnection(jdbcUrl);
            
            String sql = "INSERT INTO raw_telemetry " +
                    "(event_time, driver_number, speed, throttle, brake, rpm, gear, drs) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            
            LOG.info("ClickHouse connection opened for raw_telemetry: {}", jdbcUrl);
        }
        
        @Override
        public void invoke(CarTelemetryEvent event, Context context) throws Exception {
            stmt.setTimestamp(1, new Timestamp(event.getEventTime()));
            stmt.setInt(2, event.getDriverNumber());
            stmt.setDouble(3, event.getSpeed());
            
            // Nullable fields
            if (event.getThrottle() != null) {
                stmt.setDouble(4, event.getThrottle());
            } else {
                stmt.setNull(4, java.sql.Types.DOUBLE);
            }
            
            if (event.getBrake() != null) {
                stmt.setDouble(5, event.getBrake());
            } else {
                stmt.setNull(5, java.sql.Types.DOUBLE);
            }
            
            if (event.getRpm() != null) {
                stmt.setInt(6, event.getRpm());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            if (event.getGear() != null) {
                stmt.setInt(7, event.getGear());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            if (event.getDrs() != null) {
                stmt.setInt(8, event.getDrs());
            } else {
                stmt.setNull(8, java.sql.Types.INTEGER);
            }
            
            stmt.executeUpdate();
        }
        
        @Override
        public void close() throws Exception {
            if (stmt != null) stmt.close();
            if (connection != null) connection.close();
            super.close();
            LOG.info("ClickHouse connection closed (raw_telemetry)");
        }
    }
    
    /**
     * Sink for 10s rollup aggregations
     */
    public static class RollupSink extends RichSinkFunction<TelemetryRollup> {
        private static final Logger LOG = LoggerFactory.getLogger(RollupSink.class);
        
        private final String jdbcUrl;
        private transient Connection connection;
        private transient PreparedStatement stmt;
        
        public RollupSink(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }
        
        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            connection = DriverManager.getConnection(jdbcUrl);
            
            String sql = "INSERT INTO rollup_10s " +
                    "(window_start, driver_number, avg_speed, max_speed, avg_throttle) " +
                    "VALUES (?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            
            LOG.info("ClickHouse connection opened for rollup_10s: {}", jdbcUrl);
        }
        
        @Override
        public void invoke(TelemetryRollup rollup, Context context) throws Exception {
            stmt.setTimestamp(1, new Timestamp(rollup.windowStart));
            stmt.setInt(2, rollup.driverNumber);
            stmt.setDouble(3, rollup.avgSpeed);
            stmt.setDouble(4, rollup.maxSpeed);
            stmt.setDouble(5, rollup.avgThrottle);
            
            stmt.executeUpdate();
        }
        
        @Override
        public void close() throws Exception {
            if (stmt != null) stmt.close();
            if (connection != null) connection.close();
            super.close();
            LOG.info("ClickHouse connection closed (rollup_10s)");
        }
    }
}
