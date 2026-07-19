package com.taobao.behavior.aggregation;

import com.taobao.behavior.avro.UserBehaviorEvent;
import org.apache.flink.api.common.functions.AggregateFunction;

public class ItemMetricsAggregator
        implements AggregateFunction<UserBehaviorEvent, ItemMetricsAccumulator, ItemMetricsAccumulator> {

    @Override
    public ItemMetricsAccumulator createAccumulator() {
        return new ItemMetricsAccumulator();
    }

    @Override
    public ItemMetricsAccumulator add(UserBehaviorEvent event, ItemMetricsAccumulator accumulator) {
        if (accumulator.categoryId == 0) {
            accumulator.categoryId = event.getCategoryId();
        } else if (accumulator.categoryId != event.getCategoryId()) {
            throw new IllegalArgumentException("one item must not change category within a window");
        }

        switch (event.getBehaviorType()) {
            case pv:
                accumulator.pvCount++;
                break;
            case cart:
                accumulator.cartCount++;
                break;
            case fav:
                accumulator.favCount++;
                break;
            case buy:
                accumulator.buyCount++;
                break;
            default:
                throw new IllegalArgumentException("unsupported behavior type");
        }
        accumulator.userIds.add(event.getUserId());
        return accumulator;
    }

    @Override
    public ItemMetricsAccumulator getResult(ItemMetricsAccumulator accumulator) {
        return accumulator;
    }

    @Override
    public ItemMetricsAccumulator merge(
            ItemMetricsAccumulator left, ItemMetricsAccumulator right) {
        if (left.categoryId == 0) {
            left.categoryId = right.categoryId;
        } else if (right.categoryId != 0 && left.categoryId != right.categoryId) {
            throw new IllegalArgumentException("cannot merge different categories for one item");
        }
        left.pvCount += right.pvCount;
        left.cartCount += right.cartCount;
        left.favCount += right.favCount;
        left.buyCount += right.buyCount;
        left.userIds.addAll(right.userIds);
        return left;
    }
}
