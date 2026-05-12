package org.cloud.source;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.cloud.Constant;

import java.util.Properties;

/**
 * Kafka Source factory dùng chung cho Flink job.
 *
 * Tên class giữ nguyên để không phá build cũ; method {@link #createKafkaConsumer()}
 * vẫn dành riêng cho topic Pillar 2 ({@code fuel-prices}). Phase 4 bổ sung
 * {@link #createSourceForTopic(String, String)} dùng được cho mọi topic
 * (Pillar 3: grid-load, Pillar 4: renewable-output / emission).
 */
public class KafkaInvoiceSource {

    /** Pillar 2 — đọc từ topic {@code kafka.topic} cấu hình ở application.properties. */
    public static KafkaSource<String> createKafkaConsumer() {
        return createSourceForTopic(
                Constant.KafkaSourceConfig.TOPIC_NAME,
                Constant.KafkaSourceConfig.GROUP_ID
        );
    }

    /**
     * Tạo {@link KafkaSource} cho 1 topic bất kỳ, dùng chung config bootstrap-servers.
     *
     * @param topic   tên topic (ví dụ "grid-load")
     * @param groupId consumer group id (mỗi topic 1 group để offset độc lập)
     */
    public static KafkaSource<String> createSourceForTopic(String topic, String groupId) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers",  Constant.KafkaSourceConfig.BOOTSTRAP_SERVERS);
        props.setProperty("group.id",           groupId);
        props.setProperty("auto.offset.reset",  Constant.KafkaSourceConfig.OFFSET_RESET);
        props.setProperty("enable.auto.commit", Constant.KafkaSourceConfig.AUTO_COMMIT);

        return KafkaSource.<String>builder()
                .setProperties(props)
                .setTopics(topic)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }
}
