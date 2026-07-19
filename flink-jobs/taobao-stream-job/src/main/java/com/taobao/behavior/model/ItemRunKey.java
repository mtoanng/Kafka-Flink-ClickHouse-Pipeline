package com.taobao.behavior.model;

import java.io.Serializable;
import java.util.Objects;

public class ItemRunKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private String replayRunId;
    private long itemId;

    public ItemRunKey() {}

    public ItemRunKey(String replayRunId, long itemId) {
        this.replayRunId = replayRunId;
        this.itemId = itemId;
    }

    public String getReplayRunId() {
        return replayRunId;
    }

    public long getItemId() {
        return itemId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ItemRunKey)) {
            return false;
        }
        ItemRunKey that = (ItemRunKey) other;
        return itemId == that.itemId && Objects.equals(replayRunId, that.replayRunId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replayRunId, itemId);
    }
}
