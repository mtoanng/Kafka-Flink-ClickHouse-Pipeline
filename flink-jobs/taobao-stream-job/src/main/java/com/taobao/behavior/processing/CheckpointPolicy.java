package com.taobao.behavior.processing;

import java.io.Serializable;

public final class CheckpointPolicy implements Serializable {
    private final boolean enabled;
    private final long intervalMs;
    private final String storagePath;

    private CheckpointPolicy(boolean enabled, long intervalMs, String storagePath) {
        this.enabled = enabled;
        this.intervalMs = intervalMs;
        this.storagePath = storagePath;
    }

    public static CheckpointPolicy fromValues(
            String enabledValue, String intervalValue, String storagePath) {
        if (!"true".equalsIgnoreCase(enabledValue) && !"false".equalsIgnoreCase(enabledValue)) {
            throw new IllegalArgumentException("FLINK_CHECKPOINTING_ENABLED must be true or false");
        }
        boolean enabled = Boolean.parseBoolean(enabledValue);
        long intervalMs;
        try {
            intervalMs = Long.parseLong(intervalValue);
        } catch (NumberFormatException exc) {
            throw new IllegalArgumentException("FLINK_CHECKPOINT_INTERVAL_MS must be an integer", exc);
        }
        if (intervalMs < 1_000L) {
            throw new IllegalArgumentException("checkpoint interval must be at least 1000 ms");
        }
        if (enabled && (storagePath == null || storagePath.isBlank())) {
            throw new IllegalArgumentException(
                    "FLINK_CHECKPOINT_DIR is required when checkpointing is enabled");
        }
        return new CheckpointPolicy(enabled, intervalMs, storagePath == null ? "" : storagePath);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
