package com.f1telemetry.sink;

import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * ClickHouse JDBC sinks with batching.
 *
 * Connects to ClickHouse Cloud via HTTPS (jdbc:ch://host:8443/db?ssl=true).
 * Batches INSERTs for performance over the network — critical for cloud latency.
 */
public class ClickHouseSink {

    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * Sink for raw telemetry events → raw_telemetry table.
     */
    public static class RawTelemetrySink extends RichSinkFunction<CarTelemetryEvent> {
        private static final Logger LOG = LoggerFactory.getLogger(RawTelemetrySink.class);

        private final String jdbcUrl;
        private final String user;
        private final String password;
        private final int batchSize;

        private transient Connection conn;
        private transient PreparedStatement stmt;
        private transient int count;

        public RawTelemetrySink(String jdbcUrl, String user, String password) {
            this(jdbcUrl, user, password, DEFAULT_BATCH_SIZE);
        }

        public RawTelemetrySink(String jdbcUrl, String user, String password, int batchSize) {
            this.jdbcUrl = jdbcUrl;
            this.user = user;
            this.password = password;
            this.batchSize = batchSize;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            conn = DriverManager.getConnection(jdbcUrl, user, password);
            stmt = conn.prepareStatement(
                    "INSERT INTO raw_telemetry " +
                    "(event_time, driver_number, speed, throttle, brake, rpm, gear, drs) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            count = 0;
            LOG.info("ClickHouse raw sink ready (batch={}, url={})", batchSize,
                    jdbcUrl.replaceAll("password=[^&]*", "password=***"));
        }

        @Override
        public void invoke(CarTelemetryEvent event, Context context) throws Exception {
            stmt.setTimestamp(1, new Timestamp(event.getEventTime()));
            stmt.setInt(2, event.getDriverNumber());
            stmt.setDouble(3, event.getSpeed());
            setNullableDouble(stmt, 4, event.getThrottle());
            setNullableDouble(stmt, 5, event.getBrake());
            setNullableInt(stmt, 6, event.getRpm());
            setNullableInt(stmt, 7, event.getGear());
            setNullableInt(stmt, 8, event.getDrs());
            stmt.addBatch();
            count++;

            if (count >= batchSize) {
                stmt.executeBatch();
                LOG.debug("Flushed {} raw records", count);
                count = 0;
            }
        }

        @Override
        public void close() throws Exception {
            if (stmt != null) {
                if (count > 0) stmt.executeBatch();
                stmt.close();
            }
            if (conn != null) conn.close();
            super.close();
            LOG.info("ClickHouse raw sink closed (final flush: {} records)", count);
        }
    }

    /**
     * Sink for 10s rollup → rollup_10s table.
     */
    public static class RollupSink extends RichSinkFunction<TelemetryRollup> {
        private static final Logger LOG = LoggerFactory.getLogger(RollupSink.class);

        private final String jdbcUrl;
        private final String user;
        private final String password;
        private final int batchSize;

        private transient Connection conn;
        private transient PreparedStatement stmt;
        private transient int count;

        public RollupSink(String jdbcUrl, String user, String password) {
            this(jdbcUrl, user, password, 100);
        }

        public RollupSink(String jdbcUrl, String user, String password, int batchSize) {
            this.jdbcUrl = jdbcUrl;
            this.user = user;
            this.password = password;
            this.batchSize = batchSize;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            conn = DriverManager.getConnection(jdbcUrl, user, password);
            stmt = conn.prepareStatement(
                    "INSERT INTO rollup_10s " +
                    "(window_start, driver_number, avg_speed, max_speed, " +
                    "avg_throttle, hard_brake_count, sample_count) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");
            count = 0;
            LOG.info("ClickHouse rollup sink ready (batch={})", batchSize);
        }

        @Override
        public void invoke(TelemetryRollup r, Context context) throws Exception {
            stmt.setTimestamp(1, new Timestamp(r.windowStart));
            stmt.setInt(2, r.driverNumber);
            stmt.setDouble(3, r.avgSpeed);
            stmt.setDouble(4, r.maxSpeed);
            stmt.setDouble(5, r.avgThrottle);
            stmt.setLong(6, r.hardBrakeCount);
            stmt.setLong(7, r.sampleCount);
            stmt.addBatch();
            count++;

            if (count >= batchSize) {
                stmt.executeBatch();
                LOG.debug("Flushed {} rollup records", count);
                count = 0;
            }
        }

        @Override
        public void close() throws Exception {
            if (stmt != null) {
                if (count > 0) stmt.executeBatch();
                stmt.close();
            }
            if (conn != null) conn.close();
            super.close();
            LOG.info("ClickHouse rollup sink closed (final flush: {} records)", count);
        }
    }

    // ── Nullable JDBC helpers ───

    private static void setNullableDouble(PreparedStatement stmt, int idx, Double val)
            throws Exception {
        if (val == null) stmt.setNull(idx, Types.DOUBLE);
        else stmt.setDouble(idx, val);
    }

    private static void setNullableInt(PreparedStatement stmt, int idx, Integer val)
            throws Exception {
        if (val == null) stmt.setNull(idx, Types.INTEGER);
        else stmt.setInt(idx, val);
    }
}
