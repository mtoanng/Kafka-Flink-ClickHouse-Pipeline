package com.taobao.behavior.model;

import java.io.Serializable;

/** Bounded cart-abandonment alert emitted by the Phase 6 control-plane rule. */
public final class BehaviorAlert implements Serializable {
    private final long userId;
    private final long itemId;
    private final long categoryId;
    private final long cartEventTimeMs;
    private final long alertTimeMs;
    private final String ruleId;
    private final long ruleVersion;

    public BehaviorAlert(
            long userId,
            long itemId,
            long categoryId,
            long cartEventTimeMs,
            long alertTimeMs,
            String ruleId,
            long ruleVersion) {
        this.userId = userId;
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.cartEventTimeMs = cartEventTimeMs;
        this.alertTimeMs = alertTimeMs;
        this.ruleId = ruleId;
        this.ruleVersion = ruleVersion;
    }

    public long getUserId() { return userId; }
    public long getItemId() { return itemId; }
    public long getCategoryId() { return categoryId; }
    public long getCartEventTimeMs() { return cartEventTimeMs; }
    public long getAlertTimeMs() { return alertTimeMs; }
    public String getRuleId() { return ruleId; }
    public long getRuleVersion() { return ruleVersion; }

    @Override
    public String toString() {
        return "BehaviorAlert{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", categoryId=" + categoryId +
                ", cartEventTimeMs=" + cartEventTimeMs +
                ", alertTimeMs=" + alertTimeMs +
                ", ruleId='" + ruleId + '\'' +
                ", ruleVersion=" + ruleVersion +
                '}';
    }
}
