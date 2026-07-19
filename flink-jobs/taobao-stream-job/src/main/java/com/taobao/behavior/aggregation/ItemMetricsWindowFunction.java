package com.taobao.behavior.aggregation;

import com.taobao.behavior.model.ItemMetrics1m;
import com.taobao.behavior.model.ItemRunKey;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class ItemMetricsWindowFunction
        extends ProcessWindowFunction<ItemMetricsAccumulator, ItemMetrics1m, ItemRunKey, TimeWindow> {

    @Override
    public void process(
            ItemRunKey key,
            Context context,
            Iterable<ItemMetricsAccumulator> accumulators,
            Collector<ItemMetrics1m> output) {
        output.collect(
                toMetrics(
                        key.getReplayRunId(),
                        key.getItemId(),
                        context.window().getStart(),
                        accumulators.iterator().next()));
    }

    public static ItemMetrics1m toMetrics(
            String replayRunId, long itemId, long windowStart, ItemMetricsAccumulator accumulator) {
        return new ItemMetrics1m(
                windowStart,
                itemId,
                accumulator.categoryId,
                accumulator.pvCount,
                accumulator.cartCount,
                accumulator.favCount,
                accumulator.buyCount,
                accumulator.userIds.size(),
                replayRunId);
    }
}
