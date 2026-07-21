package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.taobao.behavior.avro.BehaviorType;
import com.taobao.behavior.model.CartItemState;
import com.taobao.behavior.model.CartLifecycleTransition;
import com.taobao.behavior.model.CartMutation;
import com.taobao.behavior.processing.ActiveCartProjector;
import org.junit.jupiter.api.Test;

class ActiveCartProjectorTest {
    @Test
    void cartCreatesItemAndRepeatedCartIsIdempotent() {
        CartLifecycleTransition first = ActiveCartProjector.transition(null,
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 10_000L, 1L));

        assertEquals(CartMutation.Type.UPSERT_CART_ITEM, first.getMutation().orElseThrow().getType());
        assertEquals(500L, first.getMutation().orElseThrow().getItem().getItemId());
        assertNull(ActiveCartProjector.transition(first.getNextState(),
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 10_000L, 1L)));
    }

    @Test
    void buyDeletesMatchingItemAndRejectsStaleCart() {
        CartLifecycleTransition cart = ActiveCartProjector.transition(null,
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 10_000L, 1L));
        CartLifecycleTransition buy = ActiveCartProjector.transition(cart.getNextState(),
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.buy, 20_000L, 2L));

        assertEquals(CartMutation.Type.DELETE_CART_ITEM, buy.getMutation().orElseThrow().getType());
        assertNull(ActiveCartProjector.transition(buy.getNextState(),
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 15_000L, 3L)));
        assertNull(ActiveCartProjector.transition(buy.getNextState(),
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.buy, 20_000L, 2L)));
    }

    @Test
    void ignoresPvAndFavAndKeepsUsersAndItemsIndependent() {
        assertNull(ActiveCartProjector.transition(null,
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.pv, 10_000L, 1L)));
        assertNull(ActiveCartProjector.transition(null,
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.fav, 10_000L, 1L)));

        CartLifecycleTransition itemA = ActiveCartProjector.transition(null,
                EventTestSupport.event(100L, 500L, 50L, BehaviorType.cart, 10_000L, 1L));
        CartLifecycleTransition itemB = ActiveCartProjector.transition(null,
                EventTestSupport.event(100L, 501L, 51L, BehaviorType.cart, 11_000L, 2L));
        CartLifecycleTransition otherUser = ActiveCartProjector.transition(null,
                EventTestSupport.event(101L, 500L, 52L, BehaviorType.cart, 12_000L, 1L));

        assertEquals(500L, itemA.getMutation().orElseThrow().getItem().getItemId());
        assertEquals(501L, itemB.getMutation().orElseThrow().getItem().getItemId());
        assertEquals(101L, otherUser.getMutation().orElseThrow().getItem().getUserId());
    }
}
