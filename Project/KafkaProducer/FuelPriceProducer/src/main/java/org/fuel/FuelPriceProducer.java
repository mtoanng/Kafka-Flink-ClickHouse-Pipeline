package org.fuel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.fuel.model.FuelPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * Java Kafka Producer cho giá nhiên liệu.
 *
 * ┌──────────────────────────────────────────────────┐
 * │  MockDataGenerator  →  Kafka  →  (Flink đọc)    │
 * └──────────────────────────────────────────────────┘
 *
 * Mỗi INTERVAL_MS, producer sinh một batch gồm 20 records
 * (4 loại nhiên liệu × 5 địa điểm) và gửi vào topic "fuel-prices".
 *
 * --- Để dùng API thực thay mock ---
 *   Người 1 chỉ cần:
 *   1. Tạo class HttpApiDataSource implements DataSource
 *   2. Sửa dòng "DataSource source = new MockDataGenerator()" bên dưới
 *   3. Không đụng gì khác
 */
public class FuelPriceProducer {

    private static final Logger log = LoggerFactory.getLogger(FuelPriceProducer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Config ────────────────────────────────────────────────────────────────
    private static String BOOTSTRAP_SERVERS;
    private static String TOPIC_NAME;
    private static long INTERVAL_MS;

    static {
        try {
            PropertiesConfiguration cfg = new PropertiesConfiguration("application.properties");
            BOOTSTRAP_SERVERS = cfg.getString("kafka.bootstrap.servers", "localhost:9092");
            TOPIC_NAME        = cfg.getString("kafka.topic",             "fuel-prices");
            INTERVAL_MS       = cfg.getLong("producer.interval.ms", 10_000L);
        } catch (Exception e) {
            log.warn("Không đọc được application.properties, dùng giá trị mặc định", e);
            BOOTSTRAP_SERVERS = "localhost:9092";
            TOPIC_NAME        = "fuel-prices";
            INTERVAL_MS       = 10_000L;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        log.info("=== Fuel Price Producer khởi động ===");
        log.info("Kafka: {} | Topic: {} | Interval: {}ms", BOOTSTRAP_SERVERS, TOPIC_NAME, INTERVAL_MS);

        MockDataGenerator generator = new MockDataGenerator();

        try (KafkaProducer<String, String> producer = buildKafkaProducer()) {

            int batchCount = 0;
            while (true) {
                batchCount++;
                List<FuelPrice> batch = generator.nextBatch();
                int sentOk = 0;

                for (FuelPrice price : batch) {
                    try {
                        String json = MAPPER.writeValueAsString(price);
                        // Key = fuelType:location → đảm bảo same key → same partition (ordering per price)
                        String key = price.getFuelType() + ":" + price.getLocation();

                        ProducerRecord<String, String> record =
                                new ProducerRecord<>(TOPIC_NAME, key, json);

                        producer.send(record, (metadata, exception) -> {
                            if (exception != null) {
                                log.error("Gửi thất bại: key={} | error={}", key, exception.getMessage());
                            }
                        });
                        sentOk++;

                    } catch (Exception e) {
                        log.error("Serialize lỗi cho {}: {}", price.getFuelType(), e.getMessage());
                    }
                }

                producer.flush(); // Đảm bảo gửi hết trước khi sleep
                log.info("[Batch #{}] Gửi {}/{} records | topic={}", batchCount, sentOk, batch.size(), TOPIC_NAME);

                Thread.sleep(INTERVAL_MS);
            }
        }
    }

    // ── Build Kafka Producer với cấu hình tối ưu ─────────────────────────────
    private static KafkaProducer<String, String> buildKafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Reliability: acks=all đảm bảo không mất message
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Retry khi lỗi mạng tạm thời
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        // Batching nhẹ để tăng throughput
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        // Compression
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new KafkaProducer<>(props);
    }
}
