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
import org.cloud.model.EmissionEvent;
import org.cloud.model.FuelPrice;
import org.cloud.model.GridLoadEvent;
import org.cloud.model.PriceAlert;
import org.cloud.model.RenewableOutputEvent;
import org.cloud.model.RuleAlertEvent;
import org.cloud.model.WindowedFuelPrice;
import org.cloud.process.AlertDetectionFunction;
import org.cloud.process.EmissionAlertDetector;
import org.cloud.process.FuelPriceAggregator;
import org.cloud.process.GridLoadAlertDetector;
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
 * ╔════════════════════════════════════════════════════════════╗
 * ║      VES-Monitor REAL-TIME PROCESSING PIPELINE  (Phase 4)  ║
 * ╠════════════════════════════════════════════════════════════╣
 * ║  Kafka (4 topic) ──► Flink ──► PostgreSQL                 ║
 * ║                                                            ║
 * ║  7 luồng xử lý song song:                                  ║
 * ║                                                            ║
 * ║  Pillar 2 (fuel-prices):                                   ║
 * ║   1. RAW         – fuel_prices_raw                         ║
 * ║   2. WINDOW 1min – fuel_price_window_agg                   ║
 * ║   3. PRICE-CHG   – fuel_price_alerts (> 3% biến động)      ║
 * ║   4. RULE-ALERT  – alerts + recommendations (FUEL_PRICE)   ║
 * ║                                                            ║
 * ║  Pillar 3 (grid-load):                                     ║
 * ║   5. RAW         – grid_load_raw                           ║
 * ║      + RULE-ALERT (GRID_LOAD_PCT >80/>90)                   ║
 * ║                                                            ║
 * ║  Pillar 4 (renewable-output, emission):                    ║
 * ║   6. RAW         – renewable_output_raw                    ║
 * ║   7. RAW         – emission_raw                            ║
 * ║      + RULE-ALERT (EMISSION_INTENSITY >600)                 ║
 * ║                                                            ║
 * ║  Tất cả 3 detector dùng chung pattern:                     ║
 * ║   • alert_rules nạp lúc start (theo metric_type)           ║
 * ║   • cooldown 60s/rule/key (MapState)                       ║
 * ║   • CRITICAL → auto recommendations (SQL NOT EXISTS 30')   ║
 * ╚════════════════════════════════════════════════════════════╝
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

        // Sink shared cho mọi rule-alert (Pillar 2/3/4).
        addAlertSinks(ruleAlertStream, "fuel-prices");

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 5: PILLAR 3 — GRID LOAD
        //   topic grid-load → grid_load_raw + alerts (GRID_LOAD_PCT)
        // ══════════════════════════════════════════════════════════════════════
        DataStream<GridLoadEvent> gridStream = env
                .fromSource(KafkaInvoiceSource.createSourceForTopic("grid-load", "ves-grid-load-processor"),
                        WatermarkStrategy.noWatermarks(),
                        "Kafka Source: grid-load")
                .flatMap((String json, Collector<GridLoadEvent> out) -> {
                    try {
                        GridLoadEvent ev = MAPPER.readValue(json, GridLoadEvent.class);
                        if (ev.regionCode != null && ev.loadMw >= 0 && ev.capacityMw > 0) {
                            out.collect(ev);
                        } else {
                            log.warn("grid-load record không hợp lệ, bỏ qua: {}", json);
                        }
                    } catch (Exception e) {
                        log.error("grid-load JSON parse lỗi: {}", json, e);
                    }
                })
                .returns(GridLoadEvent.class)
                .name("Parse Grid Load JSON");

        gridStream.map(g -> Utils.logWithTime("[GRID] " + g.toString())).print();

        gridStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO grid_load_raw " +
                "(region_code, load_mw, capacity_mw, is_peak_hour, event_time) " +
                "VALUES (?, ?, ?, ?, ?)",
                (ps, g) -> {
                    ps.setString(1,   g.regionCode);
                    ps.setDouble(2,   g.loadMw);
                    ps.setDouble(3,   g.capacityMw);
                    ps.setBoolean(4,  g.peakHour);
                    ps.setTimestamp(5, Timestamp.valueOf(g.eventTime.replace("T", " ")));
                }
        )).name("Sink: grid_load_raw");

        List<AlertRule> gridRules = AlertRulesLoader.loadRulesByMetricType("GRID_LOAD_PCT");
        log.info(" Đã nạp {} alert_rules GRID_LOAD_PCT", gridRules.size());
        if (!gridRules.isEmpty()) {
            DataStream<RuleAlertEvent> gridAlerts = gridStream
                    .keyBy(g -> g.regionCode)
                    .process(new GridLoadAlertDetector(gridRules))
                    .name("Grid Load Alert Detector");
            gridAlerts.map(a -> Utils.logWithTime("[GRID-ALERT] " + a.toString())).print();
            addAlertSinks(gridAlerts, "grid-load");
        }

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 6: PILLAR 4 — RENEWABLE OUTPUT (chỉ ghi raw, chưa có rule)
        //   topic renewable-output → renewable_output_raw
        // ══════════════════════════════════════════════════════════════════════
        DataStream<RenewableOutputEvent> renewableStream = env
                .fromSource(KafkaInvoiceSource.createSourceForTopic("renewable-output", "ves-renewable-processor"),
                        WatermarkStrategy.noWatermarks(),
                        "Kafka Source: renewable-output")
                .flatMap((String json, Collector<RenewableOutputEvent> out) -> {
                    try {
                        RenewableOutputEvent ev = MAPPER.readValue(json, RenewableOutputEvent.class);
                        if (ev.regionCode != null && ev.sourceType != null
                                && ev.outputMw >= 0 && ev.capacityMw > 0) {
                            out.collect(ev);
                        }
                    } catch (Exception e) {
                        log.error("renewable JSON parse lỗi: {}", json, e);
                    }
                })
                .returns(RenewableOutputEvent.class)
                .name("Parse Renewable Output JSON");

        renewableStream.map(r -> Utils.logWithTime("[RENEW] " + r.toString())).print();

        renewableStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO renewable_output_raw " +
                "(region_code, source_type, output_mw, capacity_mw, event_time) " +
                "VALUES (?, ?, ?, ?, ?)",
                (ps, r) -> {
                    ps.setString(1,   r.regionCode);
                    ps.setString(2,   r.sourceType);
                    ps.setDouble(3,   r.outputMw);
                    ps.setDouble(4,   r.capacityMw);
                    ps.setTimestamp(5, Timestamp.valueOf(r.eventTime.replace("T", " ")));
                }
        )).name("Sink: renewable_output_raw");

        // ══════════════════════════════════════════════════════════════════════
        // LUỒNG 7: PILLAR 4 — EMISSION
        //   topic emission → emission_raw + alerts (EMISSION_INTENSITY)
        // ══════════════════════════════════════════════════════════════════════
        DataStream<EmissionEvent> emissionStream = env
                .fromSource(KafkaInvoiceSource.createSourceForTopic("emission", "ves-emission-processor"),
                        WatermarkStrategy.noWatermarks(),
                        "Kafka Source: emission")
                .flatMap((String json, Collector<EmissionEvent> out) -> {
                    try {
                        EmissionEvent ev = MAPPER.readValue(json, EmissionEvent.class);
                        if (ev.regionCode != null && ev.co2Kg >= 0 && ev.energyMwh >= 0) {
                            out.collect(ev);
                        }
                    } catch (Exception e) {
                        log.error("emission JSON parse lỗi: {}", json, e);
                    }
                })
                .returns(EmissionEvent.class)
                .name("Parse Emission JSON");

        emissionStream.map(e -> Utils.logWithTime("[EMIT] " + e.toString())).print();

        emissionStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO emission_raw " +
                "(region_code, co2_kg, energy_mwh, event_time) " +
                "VALUES (?, ?, ?, ?)",
                (ps, e) -> {
                    ps.setString(1,   e.regionCode);
                    ps.setDouble(2,   e.co2Kg);
                    ps.setDouble(3,   e.energyMwh);
                    ps.setTimestamp(4, Timestamp.valueOf(e.eventTime.replace("T", " ")));
                }
        )).name("Sink: emission_raw");

        List<AlertRule> emissionRules = AlertRulesLoader.loadRulesByMetricType("EMISSION_INTENSITY");
        log.info(" Đã nạp {} alert_rules EMISSION_INTENSITY", emissionRules.size());
        if (!emissionRules.isEmpty()) {
            DataStream<RuleAlertEvent> emissionAlerts = emissionStream
                    .keyBy(e -> e.regionCode)
                    .process(new EmissionAlertDetector(emissionRules))
                    .name("Emission Alert Detector");
            emissionAlerts.map(a -> Utils.logWithTime("[EMIT-ALERT] " + a.toString())).print();
            addAlertSinks(emissionAlerts, "emission");
        }

        // ── Khởi chạy pipeline ──────────────────────────────────────────────
        env.execute("VES-Monitor Real-time Pipeline (4 pillars / 7 streams)");
    }

    /**
     * Đính kèm 2 sink chuẩn cho 1 luồng {@link RuleAlertEvent}:
     *   1. alerts        — mọi cảnh báo (audit trail).
     *   2. recommendations — chỉ CRITICAL, dedup SQL-level 30 phút theo title.
     *
     * Cấu trúc INSERT dùng được cho mọi pillar (FUEL_PRICE / GRID_LOAD_PCT /
     * EMISSION_INTENSITY) vì lấy metric_type + pillar trực tiếp từ event.
     */
    private static void addAlertSinks(DataStream<RuleAlertEvent> ruleAlertStream,
                                      String labelSuffix) {
        ruleAlertStream.addSink(JdbcPostgresSink.createJdbcSink(
                "INSERT INTO alerts " +
                "(rule_id, metric_type, fuel_type, location, region, triggered_price, threshold, " +
                " operator, severity, message, event_timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, a) -> {
                    ps.setLong(1,     a.ruleId);
                    ps.setString(2,   a.metricType);
                    if (a.fuelType == null) {
                        ps.setNull(3, java.sql.Types.VARCHAR);
                    } else {
                        ps.setString(3, a.fuelType);
                    }
                    ps.setString(4,   a.location);
                    ps.setString(5,   a.region);
                    ps.setDouble(6,   a.triggeredPrice);
                    ps.setDouble(7,   a.threshold);
                    ps.setString(8,   a.operator);
                    ps.setString(9,   a.severity);
                    ps.setString(10,  a.message);
                    ps.setTimestamp(11, Timestamp.valueOf(a.eventTimestamp.replace("T", " ")));
                }
        )).name("Sink: alerts (" + labelSuffix + ")");

        ruleAlertStream
                .filter(a -> "CRITICAL".equals(a.severity))
                .addSink(JdbcPostgresSink.createJdbcSink(
                        "INSERT INTO recommendations " +
                        "(pillar, action_type, severity, title, message, suggested_data, status, expires_at) " +
                        "SELECT ?::SMALLINT, ?, ?, ?, ?, ?::jsonb, 'PENDING', NOW() + INTERVAL '24 hours' " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM recommendations " +
                        "  WHERE title = ? AND suggested_at > NOW() - INTERVAL '30 minutes'" +
                        ")",
                        (ps, a) -> {
                            String title = a.recommendationTitle();
                            String json  = String.format(
                                    "{\"rule_id\":%d,\"metric_type\":\"%s\",\"fuel_type\":%s," +
                                    "\"location\":\"%s\",\"region\":\"%s\"," +
                                    "\"triggered_value\":%.4f,\"threshold\":%.4f,\"operator\":\"%s\"}",
                                    a.ruleId, a.metricType,
                                    a.fuelType == null ? "null" : "\"" + a.fuelType + "\"",
                                    a.location == null ? "" : a.location,
                                    a.region   == null ? "" : a.region,
                                    a.triggeredPrice, a.threshold, a.operator);
                            ps.setShort(1, a.getPillar());
                            ps.setString(2, a.actionTypeForRecommendation());
                            ps.setString(3, a.severity);
                            ps.setString(4, title);
                            ps.setString(5, a.message);
                            ps.setString(6, json);
                            ps.setString(7, title); // WHERE NOT EXISTS
                        }
        )).name("Sink: recommendations (" + labelSuffix + ", CRITICAL)");
    }
}
