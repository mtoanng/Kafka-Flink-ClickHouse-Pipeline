package vn.edu.ves.grid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.grid.model.GridLoadEvent;

import java.util.List;
import java.util.Properties;

/**
 * Pillar 3 Producer — đẩy phụ tải lưới điện 3 vùng VN vào Kafka topic "grid-load".
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │  GridLoadMockSource → Kafka(topic=grid-load) → Flink/Sink   │
 * └─────────────────────────────────────────────────────────────┘
 *
 * Mỗi tick (default 5s) sinh 3 records (1 record / vùng).
 * Key = region_code → đảm bảo ordering trong cùng partition.
 */
public class GridLoadGenerator {

    private static final Logger log = LoggerFactory.getLogger(GridLoadGenerator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String BOOTSTRAP_SERVERS;
    private static String TOPIC_NAME;
    private static long   INTERVAL_MS;

    static {
        try {
            PropertiesConfiguration cfg = new PropertiesConfiguration("application.properties");
            BOOTSTRAP_SERVERS = cfg.getString("kafka.bootstrap.servers", "localhost:9092");
            TOPIC_NAME        = cfg.getString("kafka.topic",             "grid-load");
            INTERVAL_MS       = cfg.getLong("producer.interval.ms",   5_000L);
        } catch (Exception e) {
            log.warn("Không đọc được application.properties, dùng default", e);
            BOOTSTRAP_SERVERS = "localhost:9092";
            TOPIC_NAME        = "grid-load";
            INTERVAL_MS       = 5_000L;
        }
        String sysProp = System.getProperty("producer.interval.ms");
        if (sysProp != null) {
            try {
                INTERVAL_MS = Long.parseLong(sysProp);
                log.info("Override interval qua system property: {}ms", INTERVAL_MS);
            } catch (NumberFormatException ignored) { /* keep default */ }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        log.info("=== Grid Load Generator (Pillar 3) khởi động ===");
        log.info("Kafka: {} | Topic: {} | Interval: {}ms",
                BOOTSTRAP_SERVERS, TOPIC_NAME, INTERVAL_MS);

        GridLoadMockSource source = new GridLoadMockSource();

        try (KafkaProducer<String, String> producer = buildKafkaProducer()) {
            int batchCount = 0;
            while (true) {
                batchCount++;
                List<GridLoadEvent> batch = source.nextBatch();
                int sentOk = 0;

                for (GridLoadEvent ev : batch) {
                    try {
                        String json = MAPPER.writeValueAsString(ev);
                        String key  = ev.getRegionCode();
                        ProducerRecord<String, String> record =
                                new ProducerRecord<>(TOPIC_NAME, key, json);
                        producer.send(record, (metadata, exception) -> {
                            if (exception != null) {
                                log.error("Send fail [{}]: {}", key, exception.getMessage());
                            }
                        });
                        sentOk++;
                    } catch (Exception ex) {
                        log.error("Serialize fail [{}]: {}", ev.getRegionCode(), ex.getMessage());
                    }
                }

                producer.flush();
                log.info("[Batch #{}] gửi {}/{} | topic={} | peak={}",
                        batchCount, sentOk, batch.size(), TOPIC_NAME,
                        batch.isEmpty() ? "n/a" : batch.get(0).isPeakHour());

                Thread.sleep(INTERVAL_MS);
            }
        }
    }

    private static KafkaProducer<String, String> buildKafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 8192);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return new KafkaProducer<>(props);
    }
}
