package com.taobao.behavior.processing;

import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.ActiveCartItem;
import com.taobao.behavior.model.CartItemState;
import com.taobao.behavior.model.CartLifecycleTransition;
import com.taobao.behavior.model.CartMutation;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class ActiveCartProjector extends KeyedProcessFunction<Long, UserBehaviorEvent, CartMutation> {
    private transient MapState<Long, CartItemState> itemStates;

    @Override
    public void open(Configuration parameters) {
        itemStates = getRuntimeContext().getMapState(new MapStateDescriptor<>(
                "user-active-cart-items", Long.class, CartItemState.class));
    }

    @Override
    public void processElement(UserBehaviorEvent event, Context context, Collector<CartMutation> output)
            throws Exception {
        CartLifecycleTransition transition = transition(itemStates.get(event.getItemId()), event);
        if (transition == null) {
            return;
        }
        itemStates.put(event.getItemId(), transition.getNextState());
        transition.getMutation().ifPresent(output::collect);
    }

    public static CartLifecycleTransition transition(CartItemState current, UserBehaviorEvent event) {
        if (!isCartOrBuy(event) || isStale(current, event)) {
            return null;
        }
        boolean cart = "cart".equals(event.getBehaviorType().toString());
        long eventTime = event.getEventTimeMs();
        CartItemState next = new CartItemState(
                cart,
                event.getCategoryId(),
                cart && (current == null || !current.isActive()) ? eventTime : current.getAddedAtMs(),
                eventTime,
                eventTime,
                event.getSourceSequence());
        ActiveCartItem item = new ActiveCartItem(
                event.getUserId(), event.getItemId(), next.getCategoryId(), next.getAddedAtMs(), eventTime);
        if (cart && current != null && current.isActive()
                && current.getLastEventTimeMs() == eventTime
                && current.getLastSourceSequence() == event.getSourceSequence()) {
            return null;
        }
        return new CartLifecycleTransition(
                next, new CartMutation(cart ? CartMutation.Type.UPSERT_CART_ITEM : CartMutation.Type.DELETE_CART_ITEM, item));
    }

    private static boolean isCartOrBuy(UserBehaviorEvent event) {
        String behavior = event.getBehaviorType().toString();
        return "cart".equals(behavior) || "buy".equals(behavior);
    }

    private static boolean isStale(CartItemState current, UserBehaviorEvent event) {
        return current != null && (event.getEventTimeMs() < current.getLastEventTimeMs()
                || (event.getEventTimeMs() == current.getLastEventTimeMs()
                && event.getSourceSequence() <= current.getLastSourceSequence()));
    }
}
