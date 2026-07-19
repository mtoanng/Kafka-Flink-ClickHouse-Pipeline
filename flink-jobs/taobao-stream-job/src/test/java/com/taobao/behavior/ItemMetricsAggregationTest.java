package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.taobao.behavior.aggregation.ItemMetricsAccumulator;
import com.taobao.behavior.aggregation.ItemMetricsAggregator;
import com.taobao.behavior.aggregation.ItemMetricsWindowFunction;
import com.taobao.behavior.avro.BehaviorType;
import com.taobao.behavior.model.ItemMetrics1m;
import org.junit.jupiter.api.Test;

class ItemMetricsAggregationTest {
    @Test
    void countsBehaviorsAndDistinctUsersForOneItemMinute() {
        ItemMetricsAggregator aggregator = new ItemMetricsAggregator();
        ItemMetricsAccumulator accumulator = aggregator.createAccumulator();
        accumulator = aggregator.add(
                EventTestSupport.event(1L, 500L, 50L, BehaviorType.pv, 10_000L, 0L),
                accumulator);
        accumulator = aggregator.add(
                EventTestSupport.event(1L, 500L, 50L, BehaviorType.cart, 20_000L, 1L),
                accumulator);
        accumulator = aggregator.add(
                EventTestSupport.event(2L, 500L, 50L, BehaviorType.fav, 30_000L, 2L),
                accumulator);
        accumulator = aggregator.add(
                EventTestSupport.event(3L, 500L, 50L, BehaviorType.buy, 40_000L, 3L),
                accumulator);
        accumulator = aggregator.add(
                EventTestSupport.event(2L, 500L, 50L, BehaviorType.pv, 50_000L, 4L),
                accumulator);

        ItemMetrics1m metrics =
                ItemMetricsWindowFunction.toMetrics("test-run", 500L, 0L, accumulator);

        assertEquals(0L, metrics.getWindowStart());
        assertEquals(500L, metrics.getItemId());
        assertEquals(50L, metrics.getCategoryId());
        assertEquals(2L, metrics.getPvCount());
        assertEquals(1L, metrics.getCartCount());
        assertEquals(1L, metrics.getFavCount());
        assertEquals(1L, metrics.getBuyCount());
        assertEquals(3L, metrics.getUniqueUsers());
        assertEquals("test-run", metrics.getReplayRunId());
    }
}
