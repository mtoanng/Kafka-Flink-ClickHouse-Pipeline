package org.cloud.process;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.cloud.model.FuelPrice;
import org.cloud.model.WindowedFuelPrice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Hàm tổng hợp (aggregate) giá nhiên liệu trong một window.
 *
 * Input:  FuelPrice  (từng record trong window)
 * Output: WindowedFuelPrice (avg, min, max, count)
 *
 * Sử dụng trong:
 *   stream.keyBy(...).window(...).aggregate(new FuelPriceAggregator(), new WindowResultMapper())
 */
public class FuelPriceAggregator
        implements AggregateFunction<FuelPrice, FuelPriceAggregator.Accumulator, FuelPriceAggregator.Accumulator> {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ── Accumulator: trạng thái trung gian trong window ─────────────────────
    public static class Accumulator {
        String fuelType;
        String location;
        String region;
        double sum   = 0;
        double min   = Double.MAX_VALUE;
        double max   = Double.MIN_VALUE;
        long   count = 0;
    }

    @Override
    public Accumulator createAccumulator() {
        return new Accumulator();
    }

    @Override
    public Accumulator add(FuelPrice value, Accumulator acc) {
        acc.fuelType = value.fuelType;
        acc.location = value.location;
        acc.region   = value.region;
        acc.sum     += value.price;
        acc.min      = Math.min(acc.min, value.price);
        acc.max      = Math.max(acc.max, value.price);
        acc.count++;
        return acc;
    }

    @Override
    public Accumulator getResult(Accumulator acc) {
        return acc; // WindowResultMapper sẽ convert sang WindowedFuelPrice
    }

    @Override
    public Accumulator merge(Accumulator a, Accumulator b) {
        Accumulator merged = new Accumulator();
        merged.fuelType = a.fuelType;
        merged.location = a.location;
        merged.region   = a.region;
        merged.sum      = a.sum + b.sum;
        merged.min      = Math.min(a.min, b.min);
        merged.max      = Math.max(a.max, b.max);
        merged.count    = a.count + b.count;
        return merged;
    }

    // ── Window Function: bổ sung thông tin window time ─────────────────────
    public static class WindowResultMapper
            extends ProcessWindowFunction<Accumulator, WindowedFuelPrice, String, TimeWindow> {

        @Override
        public void process(String key,
                            Context ctx,
                            Iterable<Accumulator> elements,
                            Collector<WindowedFuelPrice> out) {

            Accumulator acc = elements.iterator().next();
            if (acc.count == 0) return;

            String windowStart = toDateTime(ctx.window().getStart());
            String windowEnd   = toDateTime(ctx.window().getEnd());

            out.collect(new WindowedFuelPrice(
                    windowStart, windowEnd,
                    acc.fuelType, acc.location, acc.region,
                    acc.count > 0 ? acc.sum / acc.count : 0,
                    acc.min,
                    acc.max,
                    acc.count
            ));
        }

        private String toDateTime(long epochMs) {
            return LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
                    .format(FMT);
        }
    }
}
