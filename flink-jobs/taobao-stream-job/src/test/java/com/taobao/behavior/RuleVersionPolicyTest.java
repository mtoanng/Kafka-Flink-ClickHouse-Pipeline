package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taobao.behavior.avro.BehaviorRule;
import com.taobao.behavior.processing.RuleVersionPolicy;
import org.junit.jupiter.api.Test;

class RuleVersionPolicyTest {
    @Test
    void newerVersionReplacesOlderAndStaleUpdateIsIgnored() {
        BehaviorRule first = rule(1L, true, 60);
        BehaviorRule stale = rule(1L, false, 10);
        BehaviorRule newer = rule(2L, false, 10);

        assertTrue(RuleVersionPolicy.shouldReplace(null, first));
        assertFalse(RuleVersionPolicy.shouldReplace(first, stale));
        assertTrue(RuleVersionPolicy.shouldReplace(first, newer));
        assertTrue(RuleVersionPolicy.isCartAbandonmentEnabled(first));
        assertFalse(RuleVersionPolicy.isCartAbandonmentEnabled(newer));
    }

    @Test
    void dueCalculationUsesEventTimeAndThreshold() {
        assertFalse(RuleVersionPolicy.isDue(1_000L, 60, 60_999L));
        assertTrue(RuleVersionPolicy.isDue(1_000L, 60, 61_000L));
    }

    private static BehaviorRule rule(long version, boolean enabled, int thresholdSeconds) {
        return BehaviorRule.newBuilder()
                .setRuleId("cart_abandonment")
                .setRuleType("cart_abandonment")
                .setThresholdSeconds(thresholdSeconds)
                .setEnabled(enabled)
                .setVersion(version)
                .setUpdatedAt(1_700_000_000_000L + version)
                .build();
    }
}
