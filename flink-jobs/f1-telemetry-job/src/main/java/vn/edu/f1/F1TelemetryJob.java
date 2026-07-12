package vn.edu.f1;

/**
 * F1 Telemetry Flink Job — entry point.
 *
 * Pipeline (sẽ implement đầy đủ ở Phase 5):
 *   KafkaSource[Avro] (car-telemetry-events)
 *     → watermark (event_time, 3s out-of-orderness)
 *     → keyBy(driver_number)
 *     → TumblingEventTimeWindows.of(10s).aggregate(SpeedRollupAggregator)
 *     → sink ClickHouse (raw_telemetry + rollup_10s)
 *
 * Chạy in-process (MiniCluster) qua mvn -pl flink-jobs/f1-telemetry-job exec:java.
 */
public class F1TelemetryJob {

    public static void main(String[] args) {
        // TODO Phase 5: implement pipeline end-to-end.
        System.out.println("F1TelemetryJob — placeholder. Implementation pending Phase 5.");
    }
}
