package com.taobao.behavior;

import com.taobao.behavior.aggregation.ItemMetricsAggregator;
import com.taobao.behavior.aggregation.ItemMetricsWindowFunction;
import com.taobao.behavior.avro.BehaviorRule;
import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.BehaviorAlert;
import com.taobao.behavior.model.InvalidBehaviorEvent;
import com.taobao.behavior.model.ItemMetrics1m;
import com.taobao.behavior.model.ItemRunKey;
import com.taobao.behavior.processing.ActiveCartProjector;
import com.taobao.behavior.processing.CheckpointPolicy;
import com.taobao.behavior.processing.CartAbandonmentRuleProcessor;
import com.taobao.behavior.processing.EventValidator;
import com.taobao.behavior.processing.ImmediateBoundedOutOfOrdernessGenerator;
import com.taobao.behavior.processing.LateEventRouter;
import com.taobao.behavior.sink.ClickHouseSinkFactory;
import com.taobao.behavior.sink.CassandraActiveCartSink;
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
        RuntimeProfileConfig configuration = RuntimeProfileConfig.fromEnvironment(System.getenv());
        String bootstrapServers = configuration.value("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String topic = configuration.value("KAFKA_TOPIC", "user-behavior-events");
        String consumerGroup = configuration.value("KAFKA_CONSUMER_GROUP", "taobao-stream-job");
        String schemaRegistryUrl = configuration.value(
                "SCHEMA_REGISTRY_URL", "http://localhost:8081/apis/ccompat/v7");
        String clickHouseEndpoint = configuration.value("CLICKHOUSE_ENDPOINT", "https://localhost:8443");
        String clickHouseUser = configuration.value("CLICKHOUSE_USER", "default");
        String clickHousePassword = configuration.value("CLICKHOUSE_PASSWORD", "");
        String clickHouseDatabase = configuration.value("CLICKHOUSE_DATABASE", "taobao_behavior");
        CheckpointPolicy checkpointPolicy =
                CheckpointPolicy.fromValues(
                        configuration.value("FLINK_CHECKPOINTING_ENABLED", "false"),
                        configuration.value("FLINK_CHECKPOINT_INTERVAL_MS", "60000"),
                        configuration.value("FLINK_CHECKPOINT_DIR", ""));

        StreamExecutionEnvironment execution =
                StreamExecutionEnvironment.getExecutionEnvironment();
        execution.setParallelism(Integer.parseInt(configuration.value("FLINK_PARALLELISM", "1")));
        if (checkpointPolicy.isEnabled()) {
            execution.enableCheckpointing(
                    checkpointPolicy.getIntervalMs(), CheckpointingMode.AT_LEAST_ONCE);
            execution.getCheckpointConfig().setCheckpointStorage(checkpointPolicy.getStoragePath());
        }

        KafkaSource<UserBehaviorEvent> source = KafkaSource.<UserBehaviorEvent>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(consumerGroup)
                .setProperties(configuration.kafkaProperties())
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
        if (configuration.isCassandraEnabled()) {
            onTime
                    .keyBy(UserBehaviorEvent::getUserId)
                    .process(new ActiveCartProjector())
                    .name("ProjectUserActiveCart")
                    .uid("project-user-active-cart")
                    .addSink(
                            new CassandraActiveCartSink(configuration.cassandraConfig()))
                    .name("WriteUserActiveCart")
                    .uid("write-user-active-cart");
        }
        if (configuration.isCdcEnabled()) {
            KafkaSource<BehaviorRule> rulesSource = KafkaSource.<BehaviorRule>builder()
                    .setBootstrapServers(bootstrapServers)
                    .setTopics(configuration.value("RULES_KAFKA_TOPIC", "behavior-rules"))
                    .setGroupId(configuration.value("RULES_CONSUMER_GROUP", "taobao-rule-broadcast"))
                    .setProperties(configuration.kafkaProperties())
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
            alerts.print("behavior-alert");
        }

        execution.execute("Taobao User Behavior Phase 6");
    }

}
