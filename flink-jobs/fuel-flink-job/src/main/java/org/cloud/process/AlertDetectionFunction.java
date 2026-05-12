package org.cloud.process;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.cloud.model.AlertRule;
import org.cloud.model.FuelPrice;
import org.cloud.model.RuleAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Rule-based alert detection cho fuel-prices stream.
 *
 * Quy trình:
 *   1. Lúc khởi động Flink job, `AlertRulesLoader.loadFuelPriceRules()`
 *      đọc DB và truyền danh sách rule vào function (immutable).
 *   2. Mỗi event đến: iterate qua các rule applicable (match fuelType + location),
 *      so sánh price với threshold theo operator.
 *   3. Nếu vi phạm → check cooldown (MapState<ruleId, lastAlertTsMillis>).
 *        ├── Quá cooldown → emit RuleAlertEvent + cập nhật state
 *        └── Trong cooldown → bỏ qua (tránh spam).
 *
 *   Cooldown mặc định 60_000 ms = 1 phút.
 *
 * Keying: (fuel_type :: location) — đảm bảo mỗi cặp fuel-location có
 * 1 không gian cooldown riêng (rule_id × key).
 */
public class AlertDetectionFunction
        extends KeyedProcessFunction<String, FuelPrice, RuleAlertEvent> {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AlertDetectionFunction.class);

    /** Cooldown 1 phút giữa các alert cùng (rule, key). */
    public static final long COOLDOWN_MS = 60_000L;

    private final List<AlertRule> rules;

    /** State: ruleId → lastAlertTsMillis cho mỗi key (fuel::location). */
    private transient MapState<Long, Long> cooldownState;

    public AlertDetectionFunction(List<AlertRule> rules) {
        this.rules = rules;
    }

    @Override
    public void open(Configuration parameters) {
        MapStateDescriptor<Long, Long> descriptor = new MapStateDescriptor<>(
                "rule-alert-cooldown",
                Types.LONG,
                Types.LONG
        );
        cooldownState = getRuntimeContext().getMapState(descriptor);
        log.info("AlertDetectionFunction.open() — loaded {} rules", rules.size());
    }

    @Override
    public void processElement(FuelPrice event,
                               Context ctx,
                               Collector<RuleAlertEvent> out) throws Exception {

        long now = System.currentTimeMillis();

        for (AlertRule rule : rules) {
            if (!rule.appliesTo(event))     continue;
            if (!rule.isTriggered(event.price)) continue;

            Long lastTs = cooldownState.get(rule.id);
            if (lastTs != null && (now - lastTs) < COOLDOWN_MS) {
                // Vẫn trong cooldown → bỏ qua
                continue;
            }

            RuleAlertEvent alert = new RuleAlertEvent(rule, event);
            log.warn("RULE-ALERT TRIGGERED: {}", alert);
            out.collect(alert);

            cooldownState.put(rule.id, now);
        }
    }
}
