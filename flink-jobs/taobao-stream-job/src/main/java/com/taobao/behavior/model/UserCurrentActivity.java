package com.taobao.behavior.model;

import java.io.Serializable;

public class UserCurrentActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    private long userId;
    private long lastEventTimeMs;
    private String lastBehaviorType;
    private long lastItemId;
    private long lastCategoryId;
    private long sessionEventCount;
    private long updatedAtMs;
    private long lastSourceSequence;

    public UserCurrentActivity() {}

    public UserCurrentActivity(
            long userId,
            long lastEventTimeMs,
            String lastBehaviorType,
            long lastItemId,
            long lastCategoryId,
            long sessionEventCount,
            long updatedAtMs,
            long lastSourceSequence) {
        this.userId = userId;
        this.lastEventTimeMs = lastEventTimeMs;
        this.lastBehaviorType = lastBehaviorType;
        this.lastItemId = lastItemId;
        this.lastCategoryId = lastCategoryId;
        this.sessionEventCount = sessionEventCount;
        this.updatedAtMs = updatedAtMs;
        this.lastSourceSequence = lastSourceSequence;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getLastEventTimeMs() {
        return lastEventTimeMs;
    }

    public void setLastEventTimeMs(long lastEventTimeMs) {
        this.lastEventTimeMs = lastEventTimeMs;
    }

    public String getLastBehaviorType() {
        return lastBehaviorType;
    }

    public void setLastBehaviorType(String lastBehaviorType) {
        this.lastBehaviorType = lastBehaviorType;
    }

    public long getLastItemId() {
        return lastItemId;
    }

    public void setLastItemId(long lastItemId) {
        this.lastItemId = lastItemId;
    }

    public long getLastCategoryId() {
        return lastCategoryId;
    }

    public void setLastCategoryId(long lastCategoryId) {
        this.lastCategoryId = lastCategoryId;
    }

    public long getSessionEventCount() {
        return sessionEventCount;
    }

    public void setSessionEventCount(long sessionEventCount) {
        this.sessionEventCount = sessionEventCount;
    }

    public long getUpdatedAtMs() {
        return updatedAtMs;
    }

    public void setUpdatedAtMs(long updatedAtMs) {
        this.updatedAtMs = updatedAtMs;
    }

    public long getLastSourceSequence() {
        return lastSourceSequence;
    }

    public void setLastSourceSequence(long lastSourceSequence) {
        this.lastSourceSequence = lastSourceSequence;
    }
}
