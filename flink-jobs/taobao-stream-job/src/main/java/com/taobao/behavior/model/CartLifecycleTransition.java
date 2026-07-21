package com.taobao.behavior.model;

import java.io.Serializable;
import java.util.Optional;

public class CartLifecycleTransition implements Serializable {
    private final CartItemState nextState;
    private final Optional<CartMutation> mutation;

    public CartLifecycleTransition(CartItemState nextState, CartMutation mutation) {
        this.nextState = nextState;
        this.mutation = Optional.ofNullable(mutation);
    }

    public CartItemState getNextState() { return nextState; }
    public Optional<CartMutation> getMutation() { return mutation; }
}
