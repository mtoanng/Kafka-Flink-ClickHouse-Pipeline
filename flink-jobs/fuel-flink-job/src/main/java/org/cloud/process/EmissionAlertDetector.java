package org.cloud.process;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.cloud.model.AlertRule;
import org.cloud.model.EmissionEvent;
import org.cloud.model.RuleAlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Pillar 4 — phát hiện cảnh báo theo rule EMISSION_INTENSITY.
 *
 * Cùng pattern với {@link AlertDetectionFunction} / {@link GridLoadAlertDetector}.
 * So sánh {@code intensity_kg_per_mwh = co2_kg / energy_mwh} với threshold.
 */
public class EmissionAlertDetector
        extends KeyedProcessFunction<String, EmissionEvent, RuleAlertEvent> {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(EmissionAlertDetector.class);

    public static final long COOLDOWN_MS = 60_000L;

    private final List<AlertRule> rules;
    private transient MapState<Long, Long> cooldownState;

    public EmissionAlertDetector(List<AlertRule> rules) {
        this.rules = rules;
    }

    @Override
    public void open(Configuration parameters) {
        cooldownState = getRuntimeContext().getMapState(new MapStateDescriptor<>(
                "emission-alert-cooldown", Types.LONG, Types.LONG));
        log.info("EmissionAlertDetector.open() — {} rules", rules.size());
    }

    @Override
    public void processElement(EmissionEvent event, Context ctx,
                               Collector<RuleAlertEvent> out) throws Exception {
        long now = System.currentTimeMillis();
        double intensity = event.getIntensityKgPerMwh();
        if (intensity <= 0) return; // không tính khi không có sản lượng

        for (AlertRule rule : rules) {
            if (!rule.appliesToRegion(event.regionCode)) continue;
            if (!rule.isTriggered(intensity)) continue;

            Long lastTs = cooldownState.get(rule.id);
            if (lastTs != null && (now - lastTs) < COOLDOWN_MS) continue;

            RuleAlertEvent alert = new RuleAlertEvent(rule, event);
            log.warn("EMISSION-ALERT: {}", alert);
            out.collect(alert);
            cooldownState.put(rule.id, now);
        }
    }
}
