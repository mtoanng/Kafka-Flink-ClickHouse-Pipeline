package com.taobao.behavior.model;

import java.io.Serializable;

public class ActiveCartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private long userId;
    private long itemId;
    private long categoryId;
    private long addedAtMs;
    private long lastUpdatedAtMs;

    public ActiveCartItem() {}

    public ActiveCartItem(long userId, long itemId, long categoryId, long addedAtMs, long lastUpdatedAtMs) {
        this.userId = userId;
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.addedAtMs = addedAtMs;
        this.lastUpdatedAtMs = lastUpdatedAtMs;
    }

    public long getUserId() { return userId; }
    public long getItemId() { return itemId; }
    public long getCategoryId() { return categoryId; }
    public long getAddedAtMs() { return addedAtMs; }
    public long getLastUpdatedAtMs() { return lastUpdatedAtMs; }
}
