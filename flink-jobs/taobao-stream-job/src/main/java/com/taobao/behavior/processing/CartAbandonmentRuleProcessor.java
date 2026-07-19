package com.taobao.behavior.processing;

import com.taobao.behavior.avro.BehaviorRule;
import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.BehaviorAlert;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

/** Applies versioned rule updates without redeploying the data-plane job. */
public final class CartAbandonmentRuleProcessor
        extends KeyedBroadcastProcessFunction<Long, UserBehaviorEvent, BehaviorRule, BehaviorAlert> {
    public static final MapStateDescriptor<String, BehaviorRule> RULES_STATE =
            new MapStateDescriptor<>("behavior-rules", String.class, BehaviorRule.class);

    private transient MapState<Long, UserBehaviorEvent> pendingCarts;

    @Override
    public void open(Configuration parameters) {
        pendingCarts = getRuntimeContext().getMapState(
                new MapStateDescriptor<>("pending-carts", Long.class, UserBehaviorEvent.class));
    }

    @Override
    public void processBroadcastElement(
            BehaviorRule candidate,
            Context context,
            Collector<BehaviorAlert> out) throws Exception {
        if (candidate == null || candidate.getRuleId() == null) {
            return;
        }
        BroadcastState<String, BehaviorRule> rules = context.getBroadcastState(RULES_STATE);
        String ruleId = candidate.getRuleId().toString();
        BehaviorRule existing = rules.get(ruleId);
        if (RuleVersionPolicy.shouldReplace(existing, candidate)) {
            rules.put(ruleId, candidate);
        }
    }

    @Override
    public void processElement(
            UserBehaviorEvent event,
            ReadOnlyContext context,
            Collector<BehaviorAlert> out) throws Exception {
        ReadOnlyBroadcastState<String, BehaviorRule> rules = context.getBroadcastState(RULES_STATE);
        BehaviorRule rule = rules.get("cart_abandonment");
        String behavior = event.getBehaviorType().toString();
        if ("buy".equals(behavior)) {
            pendingCarts.remove(event.getItemId());
            return;
        }
        if (!"cart".equals(behavior) || !RuleVersionPolicy.isCartAbandonmentEnabled(rule)) {
            return;
        }
        pendingCarts.put(event.getItemId(), event);
        context.timerService().registerEventTimeTimer(
                event.getEventTimeMs() + rule.getThresholdSeconds() * 1000L);
    }

    @Override
    public void onTimer(
            long timestamp,
            OnTimerContext context,
            Collector<BehaviorAlert> out) throws Exception {
        BehaviorRule rule = context.getBroadcastState(RULES_STATE).get("cart_abandonment");
        if (!RuleVersionPolicy.isCartAbandonmentEnabled(rule)) {
            pendingCarts.clear();
            return;
        }
        List<Long> expiredItems = new ArrayList<>();
        for (Map.Entry<Long, UserBehaviorEvent> entry : pendingCarts.entries()) {
            UserBehaviorEvent cart = entry.getValue();
            if (RuleVersionPolicy.isDue(
                    cart.getEventTimeMs(), rule.getThresholdSeconds(), timestamp)) {
                out.collect(new BehaviorAlert(
                        cart.getUserId(),
                        cart.getItemId(),
                        cart.getCategoryId(),
                        cart.getEventTimeMs(),
                        timestamp,
                        rule.getRuleId().toString(),
                        rule.getVersion()));
                expiredItems.add(entry.getKey());
            }
        }
        for (Long itemId : expiredItems) {
            pendingCarts.remove(itemId);
        }
    }
}
