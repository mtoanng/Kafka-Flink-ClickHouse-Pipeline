package com.taobao.behavior.processing;

import com.taobao.behavior.avro.BehaviorRule;

/** Version ordering for compacted rule updates. */
public final class RuleVersionPolicy {
    private RuleVersionPolicy() {}

    public static boolean shouldReplace(BehaviorRule existing, BehaviorRule candidate) {
        return candidate != null
                && candidate.getRuleId() != null
                && (existing == null || candidate.getVersion() > existing.getVersion());
    }

    public static boolean isCartAbandonmentEnabled(BehaviorRule rule) {
        return rule != null
                && "cart_abandonment".equals(rule.getRuleType().toString())
                && rule.getEnabled()
                && rule.getThresholdSeconds() > 0;
    }

    public static boolean isDue(long cartEventTimeMs, int thresholdSeconds, long timerTimeMs) {
        return thresholdSeconds > 0
                && timerTimeMs >= cartEventTimeMs + thresholdSeconds * 1000L;
    }
}
