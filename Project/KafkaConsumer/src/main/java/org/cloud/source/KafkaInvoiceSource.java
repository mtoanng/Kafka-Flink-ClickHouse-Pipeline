package org.cloud.source;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.cloud.Constant;

import java.util.Properties;

/**
 * Tạo Kafka Source để Flink đọc từ topic "fuel-prices".
 * Giữ tên file cũ để tránh ảnh hưởng cấu hình Maven.
 */
public class KafkaInvoiceSource {

    public static KafkaSource<String> createKafkaConsumer() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", Constant.KafkaSourceConfig.BOOTSTRAP_SERVERS);
        props.setProperty("group.id",          Constant.KafkaSourceConfig.GROUP_ID);
        props.setProperty("auto.offset.reset", Constant.KafkaSourceConfig.OFFSET_RESET);
        props.setProperty("enable.auto.commit", Constant.KafkaSourceConfig.AUTO_COMMIT);

        return KafkaSource.<String>builder()
                .setProperties(props)
                .setTopics(Constant.KafkaSourceConfig.TOPIC_NAME)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
    }
}
