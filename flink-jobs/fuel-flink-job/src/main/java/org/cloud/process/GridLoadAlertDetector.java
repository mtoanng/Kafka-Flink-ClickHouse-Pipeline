package org.cloud.process;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.cloud.model.AlertRule;
import org.cloud.model.GridLoadEvent;
import org.cloud.model.RuleAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Pillar 3 — phát hiện cảnh báo theo rule GRID_LOAD_PCT.
 *
 * Cùng pattern với {@link AlertDetectionFunction}:
 *   - Rule list nạp 1 lần lúc job start (immutable).
 *   - Match theo region_code (rule.regionCode = null → all regions).
 *   - So sánh {@code load_pct = load_mw/capacity_mw*100} với threshold.
 *   - MapState&lt;ruleId, lastAlertTsMillis&gt; — cooldown 60s/rule/key.
 *
 * Keying: region_code → mỗi region 1 cooldown space riêng.
 */
public class GridLoadAlertDetector
        extends KeyedProcessFunction<String, GridLoadEvent, RuleAlertEvent> {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(GridLoadAlertDetector.class);

    public static final long COOLDOWN_MS = 60_000L;

    private final List<AlertRule> rules;
    private transient MapState<Long, Long> cooldownState;

    public GridLoadAlertDetector(List<AlertRule> rules) {
        this.rules = rules;
    }

    @Override
    public void open(Configuration parameters) {
        cooldownState = getRuntimeContext().getMapState(new MapStateDescriptor<>(
                "grid-load-alert-cooldown", Types.LONG, Types.LONG));
        log.info("GridLoadAlertDetector.open() — {} rules", rules.size());
    }

    @Override
    public void processElement(GridLoadEvent event, Context ctx,
                               Collector<RuleAlertEvent> out) throws Exception {
        long now = System.currentTimeMillis();
        double loadPct = event.getLoadPct();

        for (AlertRule rule : rules) {
            if (!rule.appliesToRegion(event.regionCode)) continue;
            if (!rule.isTriggered(loadPct)) continue;

            Long lastTs = cooldownState.get(rule.id);
            if (lastTs != null && (now - lastTs) < COOLDOWN_MS) continue;

            RuleAlertEvent alert = new RuleAlertEvent(rule, event);
            log.warn("GRID-LOAD-ALERT: {}", alert);
            out.collect(alert);
            cooldownState.put(rule.id, now);
        }
    }
}
