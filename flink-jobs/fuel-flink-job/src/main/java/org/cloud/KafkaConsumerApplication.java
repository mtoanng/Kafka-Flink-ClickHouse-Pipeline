package org.cloud;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.cloud.model.AlertRule;
import org.cloud.model.FuelPrice;
import org.cloud.model.PriceAlert;
import org.cloud.model.RuleAlertEvent;
import org.cloud.model.WindowedFuelPrice;
import org.cloud.process.AlertDetectionFunction;
import org.cloud.process.FuelPriceAggregator;
import org.cloud.process.PriceChangeDetector;
import org.cloud.source.AlertRulesLoader;
import org.cloud.source.JdbcPostgresSink;
import org.cloud.source.KafkaInvoiceSource;
import org.cloud.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║         FUEL PRICE REAL-TIME PROCESSING PIPELINE        ║
 * ╠══════════════════════════════════════════════════════════╣
 * ║  Kafka (fuel-prices) ──► Flink ──► PostgreSQL           ║
 * ║                                                          ║
 * ║  4 luồng xử lý song song:                               ║
 * ║  1. RAW         – fuel_prices_raw                        ║
 * ║  2. WINDOW 1min – fuel_price_window_agg                  ║
 * ║  3. PRICE-CHG   – fuel_price_alerts (biến động > 3%)     ║
 * ║  4. RULE-ALERT  – alerts + recommendations (Phase 3)     ║
 * ║                   • alert_rules nạp lúc start (JDBC)     ║
 * ║                   • cooldown 1 phút/rule (MapState)      ║
 * ║                   • CRITICAL → auto recommendations       ║
 * ║                     (SQL NOT EXISTS dedup 30 phút)        ║
 * ╚══════════════════════════════════════════════════════════╝
 */
public class KafkaConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerApplication.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        // ── 1. Môi trường Flink ──────────────────────────────────────────────
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // Checkpoint mỗi 30 giây để không mất dữ liệu khi restart
        env.enableCheckpointing(30_000);

        // ── 2. Nguồn Kafka ───────────────────────────────────────────────────
        KafkaSource<String> kafkaSource = KafkaInvoiceSource.createKafkaConsumer();

        // ── 3. Parse JSON → FuelPrice ────────────────────────────────────────
        DataStream<FuelPrice> fuelStream = env
                .fromSource(kafkaSource,
                        WatermarkStrategy.noWatermarks(),
                        Constant.KafkaSourceConfig.SOURCE_NAME)
                .flatMap((String json, Collector<FuelPrice> out) -> {
                    try {
                        FuelPrice fp = MAPPER.readValue(json, FuelPrice.class);
                        // Validate cơ bản: bỏ qua record giá âm hoặc thiếu field
                        if (fp.price > 0 && fp.fuelType != null && fp.location != null) {
                            out.collect(fp);
                        } else {
                            log.warn("Record không hợp lệ bị bỏ qua: {}", json);
                        }
                    } catch (Exception e) {
                        log.error("JSON parse lỗi, bỏ qua: {}", json, e);
                    }
                })
                .returns(FuelPrice.class)
                .name("Parse Fuel Price JSON");

        fuelStream.map(fp -> Utils.logWithTime(fp.toString())).print();

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 1: RAW SINK — Lưu từng record vào bảng fuel_prices_raw
        // ══════════════════════════════════════════════════════════════════════
        fuelStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO fuel_prices_raw " +
                "(event_timestamp, fuel_type, price, price_unit, location, region, source) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                (ps, fp) -> {
                    ps.setTimestamp(1, Timestamp.valueOf(fp.timestamp.replace("T", " ")));
                    ps.setString(2,  fp.fuelType);
                    ps.setDouble(3,  fp.price);
                    ps.setString(4,  fp.priceUnit);
                    ps.setString(5,  fp.location);
                    ps.setString(6,  fp.region);
                    ps.setString(7,  fp.source);
                }
        )).name("Sink: fuel_prices_raw");

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 2: WINDOW AGGREGATION — Tổng hợp mỗi 1 phút
        //   → bảng fuel_price_window_agg
        // ══════════════════════════════════════════════════════════════════════
        SingleOutputStreamOperator<WindowedFuelPrice> windowedStream = fuelStream
                .keyBy(FuelPrice::getCompositeKey)
                .window(TumblingProcessingTimeWindows.of(
                        Time.minutes(Constant.WindowConfig.TUMBLING_WINDOW_MINUTES)))
                .aggregate(
                        new FuelPriceAggregator(),
                        new FuelPriceAggregator.WindowResultMapper()
                )
                .name("Window Aggregation (1 min)");

        windowedStream.map(w -> Utils.logWithTime("[WINDOW] " + w.toString())).print();

        windowedStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO fuel_price_window_agg " +
                "(window_start, window_end, fuel_type, location, region, avg_price, min_price, max_price, record_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, w) -> {
                    ps.setTimestamp(1, Timestamp.valueOf(w.windowStart.replace("T", " ")));
                    ps.setTimestamp(2, Timestamp.valueOf(w.windowEnd.replace("T", " ")));
                    ps.setString(3,   w.fuelType);
                    ps.setString(4,   w.location);
                    ps.setString(5,   w.region);
                    ps.setDouble(6,   w.avgPrice);
                    ps.setDouble(7,   w.minPrice);
                    ps.setDouble(8,   w.maxPrice);
                    ps.setLong(9,     w.recordCount);
                }
        )).name("Sink: fuel_price_window_agg");

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 3: PRICE CHANGE DETECTION — Phát hiện biến động > 3%
        //   → bảng fuel_price_alerts
        // ══════════════════════════════════════════════════════════════════════
        DataStream<PriceAlert> alertStream = fuelStream
                .keyBy(FuelPrice::getCompositeKey)
                .process(new PriceChangeDetector())
                .name("Price Change Detector");

        alertStream.map(a -> Utils.logWithTime("[ALERT] " + a.toString())).print();

        alertStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO fuel_price_alerts " +
                "(alert_timestamp, fuel_type, location, region, previous_price, current_price, change_percent, alert_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, a) -> {
                    ps.setTimestamp(1, Timestamp.valueOf(a.timestamp.replace("T", " ")));
                    ps.setString(2,   a.fuelType);
                    ps.setString(3,   a.location);
                    ps.setString(4,   a.region);
                    ps.setDouble(5,   a.previousPrice);
                    ps.setDouble(6,   a.currentPrice);
                    ps.setDouble(7,   a.changePercent);
                    ps.setString(8,   a.alertType);
                }
        )).name("Sink: fuel_price_alerts");

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 4: RULE-BASED ALERT DETECTION (Phase 3)
        //   → bảng alerts (audit trail mọi vi phạm rule, cooldown 1 phút)
        //   → bảng recommendations (chỉ CRITICAL, dedup 30 phút SQL-level)
        // ══════════════════════════════════════════════════════════════════════
        List<AlertRule> fuelRules = AlertRulesLoader.loadFuelPriceRules();
        log.info("════════════════════════════════════════════════════════");
        log.info(" Đã nạp {} alert_rules FUEL_PRICE từ database", fuelRules.size());
        log.info("════════════════════════════════════════════════════════");

        DataStream<RuleAlertEvent> ruleAlertStream = fuelStream
                .keyBy(fp -> fp.fuelType + "::" + fp.location)
                .process(new AlertDetectionFunction(fuelRules))
                .name("Rule-based Alert Detector");

        ruleAlertStream.map(a -> Utils.logWithTime("[RULE-ALERT] " + a.toString())).print();

        // Sink #1: bảng alerts — audit trail
        ruleAlertStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO alerts " +
                "(rule_id, fuel_type, location, region, triggered_price, threshold, " +
                " operator, severity, message, event_timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, a) -> {
                    ps.setLong(1,     a.ruleId);
                    ps.setString(2,   a.fuelType);
                    ps.setString(3,   a.location);
                    ps.setString(4,   a.region);
                    ps.setDouble(5,   a.triggeredPrice);
                    ps.setDouble(6,   a.threshold);
                    ps.setString(7,   a.operator);
                    ps.setString(8,   a.severity);
                    ps.setString(9,   a.message);
                    ps.setTimestamp(10, Timestamp.valueOf(a.eventTimestamp.replace("T", " ")));
                }
        )).name("Sink: alerts (rule-based)");

        // Sink #2: bảng recommendations — chỉ CRITICAL, dedup 30 phút SQL-level
        ruleAlertStream
                .filter(a -> "CRITICAL".equals(a.severity))
                .addSink(JdbcPostgresSink.createJdbcSink(
                    // INSERT chỉ khi không có recommendation tương tự trong 30 phút qua
                    "INSERT INTO recommendations " +
                    "(pillar, action_type, severity, title, message, suggested_data, status, expires_at) " +
                    "SELECT 2::SMALLINT, ?, ?, ?, ?, ?::jsonb, 'PENDING', NOW() + INTERVAL '24 hours' " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM recommendations " +
                    "  WHERE title = ? AND suggested_at > NOW() - INTERVAL '30 minutes'" +
                    ")",
                    (ps, a) -> {
                        String title = String.format("Auto: %s %s vượt ngưỡng tại %s",
                                a.fuelType, a.operator, a.location);
                        String json = String.format(
                                "{\"rule_id\":%d,\"fuel_type\":\"%s\",\"location\":\"%s\"," +
                                "\"triggered_price\":%.4f,\"threshold\":%.4f,\"operator\":\"%s\"}",
                                a.ruleId, a.fuelType, a.location,
                                a.triggeredPrice, a.threshold, a.operator);
                        ps.setString(1, a.actionTypeForRecommendation());
                        ps.setString(2, a.severity);
                        ps.setString(3, title);
                        ps.setString(4, a.message);
                        ps.setString(5, json);
                        ps.setString(6, title);  // cho WHERE NOT EXISTS
                    }
        )).name("Sink: recommendations (auto-gen CRITICAL)");

        // ── 4. Khởi chạy pipeline ────────────────────────────────────────────
        env.execute("Fuel Price Real-time Processor (with Rule-based Alerts)");
    }
}
