package com.taobao.behavior;

import com.taobao.behavior.aggregation.ItemMetricsAggregator;
import com.taobao.behavior.aggregation.ItemMetricsWindowFunction;
import com.taobao.behavior.avro.BehaviorRule;
import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.BehaviorAlert;
import com.taobao.behavior.model.InvalidBehaviorEvent;
import com.taobao.behavior.model.ItemMetrics1m;
import com.taobao.behavior.model.ItemRunKey;
import com.taobao.behavior.model.UserCurrentActivity;
import com.taobao.behavior.processing.CurrentActivityProjector;
import com.taobao.behavior.processing.CheckpointPolicy;
import com.taobao.behavior.processing.CartAbandonmentRuleProcessor;
import com.taobao.behavior.processing.EventValidator;
import com.taobao.behavior.processing.ImmediateBoundedOutOfOrdernessGenerator;
import com.taobao.behavior.processing.LateEventRouter;
import com.taobao.behavior.sink.ClickHouseSinkFactory;
import com.taobao.behavior.sink.ScyllaCurrentActivitySink;
import java.time.Duration;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;

public final class TaobaoStreamJob {
    static final long MAX_OUT_OF_ORDERNESS_MILLIS = 5_000L;

    private TaobaoStreamJob() {}

    public static void main(String[] args) throws Exception {
        String bootstrapServers = environment("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String topic = environment("KAFKA_TOPIC", "user-behavior-events");
        String consumerGroup = environment("KAFKA_CONSUMER_GROUP", "taobao-stream-job");
        String schemaRegistryUrl = environment("SCHEMA_REGISTRY_URL", "http://localhost:8081");
        String rulesTopic = environment("RULES_KAFKA_TOPIC", "behavior-rules");
        String rulesConsumerGroup = environment("RULES_CONSUMER_GROUP", "taobao-rule-broadcast");
        String clickHouseEndpoint = environment("CLICKHOUSE_ENDPOINT", "https://localhost:8443");
        String clickHouseUser = environment("CLICKHOUSE_USER", "default");
        String clickHousePassword = environment("CLICKHOUSE_PASSWORD", "");
        String clickHouseDatabase = environment("CLICKHOUSE_DATABASE", "taobao_behavior");
        String scyllaHost = environment("SCYLLA_HOST", "localhost");
        int scyllaPort = Integer.parseInt(environment("SCYLLA_PORT", "9042"));
        String scyllaLocalDatacenter = environment("SCYLLA_LOCAL_DATACENTER", "datacenter1");
        String scyllaUser = environment("SCYLLA_USER", "");
        String scyllaPassword = environment("SCYLLA_PASSWORD", "");
        String scyllaKeyspace = environment("SCYLLA_KEYSPACE", "taobao_behavior");
        CheckpointPolicy checkpointPolicy =
                CheckpointPolicy.fromValues(
                        environment("FLINK_CHECKPOINTING_ENABLED", "false"),
                        environment("FLINK_CHECKPOINT_INTERVAL_MS", "60000"),
                        environment("FLINK_CHECKPOINT_DIR", ""));

        StreamExecutionEnvironment execution =
                StreamExecutionEnvironment.getExecutionEnvironment();
        execution.setParallelism(Integer.parseInt(environment("FLINK_PARALLELISM", "1")));
        if (checkpointPolicy.isEnabled()) {
            execution.enableCheckpointing(
                    checkpointPolicy.getIntervalMs(), CheckpointingMode.AT_LEAST_ONCE);
            execution.getCheckpointConfig().setCheckpointStorage(checkpointPolicy.getStoragePath());
        }

        KafkaSource<UserBehaviorEvent> source = KafkaSource.<UserBehaviorEvent>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(consumerGroup)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(
                        ConfluentRegistryAvroDeserializationSchema.forSpecific(
                                UserBehaviorEvent.class, schemaRegistryUrl))
                .build();

        DataStream<UserBehaviorEvent> decoded = execution.fromSource(
                        source, WatermarkStrategy.noWatermarks(), "KafkaUserBehaviorSource")
                .uid("kafka-user-behavior-source");

        SingleOutputStreamOperator<UserBehaviorEvent> valid = decoded
                .process(new EventValidator())
                .name("ValidateBehaviorEvent")
                .uid("validate-behavior-event");
        DataStream<InvalidBehaviorEvent> invalid =
                valid.getSideOutput(EventValidator.INVALID_EVENTS);

        WatermarkStrategy<UserBehaviorEvent> watermarkStrategy = WatermarkStrategy
                .<UserBehaviorEvent>forGenerator(
                        ignored -> new ImmediateBoundedOutOfOrdernessGenerator(
                                MAX_OUT_OF_ORDERNESS_MILLIS))
                .withTimestampAssigner((event, previousTimestamp) -> event.getEventTimeMs())
                .withIdleness(Duration.ofSeconds(30));

        SingleOutputStreamOperator<UserBehaviorEvent> onTime = valid
                .assignTimestampsAndWatermarks(watermarkStrategy)
                .name("AssignEventTimeAndWatermarks")
                .uid("assign-event-time-watermarks")
                .process(new LateEventRouter())
                .name("RouteLateEvents")
                .uid("route-late-events");
        DataStream<UserBehaviorEvent> late = onTime.getSideOutput(LateEventRouter.LATE_EVENTS);

        SingleOutputStreamOperator<ItemMetrics1m> metrics = onTime
                .keyBy(event -> new ItemRunKey(event.getReplayRunId().toString(), event.getItemId()))
                .window(TumblingEventTimeWindows.of(Duration.ofMinutes(1)))
                .allowedLateness(Duration.ZERO)
                .sideOutputLateData(LateEventRouter.LATE_EVENTS)
                .aggregate(new ItemMetricsAggregator(), new ItemMetricsWindowFunction())
                .name("ItemMetrics1m")
                .uid("item-metrics-1m");

        DataStream<UserBehaviorEvent> windowLate =
                metrics.getSideOutput(LateEventRouter.LATE_EVENTS);

        SingleOutputStreamOperator<UserCurrentActivity> currentActivity = onTime
                .keyBy(UserBehaviorEvent::getUserId)
                .process(new CurrentActivityProjector())
                .name("ProjectCurrentUserActivity")
                .uid("project-current-user-activity");

        KafkaSource<BehaviorRule> rulesSource = KafkaSource.<BehaviorRule>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(rulesTopic)
                .setGroupId(rulesConsumerGroup)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(
                        ConfluentRegistryAvroDeserializationSchema.forSpecific(
                                BehaviorRule.class, schemaRegistryUrl))
                .build();
        DataStream<BehaviorRule> ruleChanges = execution.fromSource(
                        rulesSource, WatermarkStrategy.noWatermarks(), "KafkaBehaviorRulesSource")
                .uid("kafka-behavior-rules-source");
        DataStream<BehaviorAlert> alerts = onTime
                .keyBy(UserBehaviorEvent::getUserId)
                .connect(ruleChanges.broadcast(CartAbandonmentRuleProcessor.RULES_STATE))
                .process(new CartAbandonmentRuleProcessor())
                .name("ApplyBroadcastBehaviorRules")
                .uid("apply-broadcast-behavior-rules");

        valid.sinkTo(
                        ClickHouseSinkFactory.createRawSink(
                                clickHouseEndpoint,
                                clickHouseUser,
                                clickHousePassword,
                                clickHouseDatabase,
                                "raw_behavior_events"))
                .name("WriteRawBehaviorEvents")
                .uid("write-raw-behavior-events");
        invalid.print("invalid");
        late.union(windowLate).print("late");
        metrics.sinkTo(
                        ClickHouseSinkFactory.createItemMetricsSink(
                                clickHouseEndpoint,
                                clickHouseUser,
                                clickHousePassword,
                                clickHouseDatabase,
                                "item_metrics_1m"))
                .name("WriteItemMetrics1m")
                .uid("write-item-metrics-1m");
        currentActivity
                .addSink(
                        new ScyllaCurrentActivitySink(
                                scyllaHost,
                                scyllaPort,
                                scyllaLocalDatacenter,
                                scyllaUser,
                                scyllaPassword,
                                scyllaKeyspace,
                                "user_current_activity"))
                .name("WriteCurrentUserActivity")
                .uid("write-current-user-activity");
        alerts.print("behavior-alert");

        execution.execute("Taobao User Behavior Phase 6");
    }

    private static String environment(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
