package org.cloud.process;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.cloud.model.FuelPrice;
import org.cloud.model.PriceAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phát hiện biến động giá bất thường.
 *
 * Cách hoạt động:
 *  - Mỗi key (fuelType::location) có một ValueState lưu giá lần gần nhất.
 *  - Khi nhận record mới:
 *      ├── Nếu chưa có giá cũ → lưu lại, không alert
 *      └── Nếu có giá cũ → tính % thay đổi
 *              ├── |change| > THRESHOLD → emit PriceAlert
 *              └── else → bỏ qua
 *  - Luôn cập nhật state với giá mới nhất.
 *
 * Threshold: PriceAlert.ALERT_THRESHOLD_PERCENT (mặc định 3.0%)
 */
public class PriceChangeDetector
        extends KeyedProcessFunction<String, FuelPrice, PriceAlert> {

    private static final Logger log = LoggerFactory.getLogger(PriceChangeDetector.class);

    // Keyed state: giá cuối cùng của key này
    private transient ValueState<Double> lastPriceState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Double> descriptor =
                new ValueStateDescriptor<>("lastPrice", Types.DOUBLE);
        lastPriceState = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void processElement(FuelPrice event,
                               Context ctx,
                               Collector<PriceAlert> out) throws Exception {

        Double lastPrice = lastPriceState.value();

        if (lastPrice == null) {
            // Lần đầu thấy key này — chỉ lưu, không cần alert
            lastPriceState.update(event.price);
            return;
        }

        double changePercent = Math.abs(
                (event.price - lastPrice) / lastPrice * 100.0
        );

        if (changePercent >= PriceAlert.ALERT_THRESHOLD_PERCENT) {
            PriceAlert alert = new PriceAlert(
                    event.timestamp,
                    event.fuelType,
                    event.location,
                    event.region,
                    lastPrice,
                    event.price
            );
            log.warn("PRICE ALERT: {}", alert);
            out.collect(alert);
        }

        // Cập nhật state với giá mới nhất
        lastPriceState.update(event.price);
    }
}
