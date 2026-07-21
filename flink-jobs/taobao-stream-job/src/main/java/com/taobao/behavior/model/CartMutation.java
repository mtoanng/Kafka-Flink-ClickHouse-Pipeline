package com.taobao.behavior.model;

import java.io.Serializable;

public class CartMutation implements Serializable {
    public enum Type { UPSERT_CART_ITEM, DELETE_CART_ITEM }

    private final Type type;
    private final ActiveCartItem item;

    public CartMutation(Type type, ActiveCartItem item) {
        this.type = type;
        this.item = item;
    }

    public Type getType() { return type; }
    public ActiveCartItem getItem() { return item; }
}
