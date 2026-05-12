package vn.edu.ves.renewable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.renewable.model.EmissionEvent;
import vn.edu.ves.renewable.model.RenewableOutputEvent;

import java.util.List;
import java.util.Properties;

/**
 * Pillar 4 Producer — đẩy song song 2 topic:
 *   - {@code renewable-output} : solar/wind/hydro output 9 records/tick (3 region × 3 source).
 *   - {@code emission}         : phát thải CO2 3 records/tick (1/region).
 *
 * Default interval:
 *   - renewable-output: 10s
 *   - emission         : 30s  (chỉ tính khi đến lượt = mỗi 3 tick)
 *
 * Logic CO2 dùng renewable output của tick hiện tại để suy ra phần phải bù bằng
 * nhiệt điện than/khí (intensity 750 kg/MWh + noise).
 */
public class RenewableGenerator {

    private static final Logger log = LoggerFactory.getLogger(RenewableGenerator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String BOOTSTRAP_SERVERS;
    private static String RENEWABLE_TOPIC;
    private static String EMISSION_TOPIC;
    private static long   RENEWABLE_INTERVAL_MS;
    private static long   EMISSION_INTERVAL_MS;

    static {
        try {
            PropertiesConfiguration cfg = new PropertiesConfiguration("application.properties");
            BOOTSTRAP_SERVERS     = cfg.getString("kafka.bootstrap.servers", "localhost:9092");
            RENEWABLE_TOPIC       = cfg.getString("kafka.topic.renewable",   "renewable-output");
            EMISSION_TOPIC        = cfg.getString("kafka.topic.emission",    "emission");
            RENEWABLE_INTERVAL_MS = cfg.getLong("producer.renewable.interval.ms", 10_000L);
            EMISSION_INTERVAL_MS  = cfg.getLong("producer.emission.interval.ms",  30_000L);
        } catch (Exception e) {
            log.warn("Không đọc được application.properties, dùng default", e);
            BOOTSTRAP_SERVERS     = "localhost:9092";
            RENEWABLE_TOPIC       = "renewable-output";
            EMISSION_TOPIC        = "emission";
            RENEWABLE_INTERVAL_MS = 10_000L;
            EMISSION_INTERVAL_MS  = 30_000L;
        }
        String overrideRen = System.getProperty("producer.renewable.interval.ms");
        if (overrideRen != null) {
            try { RENEWABLE_INTERVAL_MS = Long.parseLong(overrideRen); } catch (NumberFormatException ignored) {}
        }
        String overrideEm = System.getProperty("producer.emission.interval.ms");
        if (overrideEm != null) {
            try { EMISSION_INTERVAL_MS = Long.parseLong(overrideEm); } catch (NumberFormatException ignored) {}
        }
    }

    public static void main(String[] args) throws InterruptedException {
        log.info("=== Renewable + Emission Generator (Pillar 4) khởi động ===");
        log.info("Kafka: {} | renewable-topic: {} ({}ms) | emission-topic: {} ({}ms)",
                BOOTSTRAP_SERVERS, RENEWABLE_TOPIC, RENEWABLE_INTERVAL_MS,
                EMISSION_TOPIC, EMISSION_INTERVAL_MS);

        RenewableMockSource source = new RenewableMockSource();
        // Lưu ý dùng double để giữ phần thập phân (30s = 0.5 phút).
        double emissionWindowMinutes = EMISSION_INTERVAL_MS / 60_000.0;
        if (emissionWindowMinutes <= 0) emissionWindowMinutes = 0.5;

        try (KafkaProducer<String, String> producer = buildKafkaProducer()) {
            int tick = 0;
            long lastEmissionTs = 0L;

            while (true) {
                tick++;
                long now = System.currentTimeMillis();

                List<RenewableOutputEvent> renewableBatch = source.nextOutputBatch();
                int renOk = sendRenewable(producer, renewableBatch);
                log.info("[Tick #{}] renewable: gửi {}/{} -> topic={}",
                        tick, renOk, renewableBatch.size(), RENEWABLE_TOPIC);

                if (now - lastEmissionTs >= EMISSION_INTERVAL_MS) {
                    List<EmissionEvent> emissionBatch =
                            source.nextEmissionBatch(renewableBatch, emissionWindowMinutes);
                    int emOk = sendEmission(producer, emissionBatch);
                    log.info("[Tick #{}] emission : gửi {}/{} -> topic={} (window={}m)",
                            tick, emOk, emissionBatch.size(), EMISSION_TOPIC,
                            String.format("%.2f", emissionWindowMinutes));
                    lastEmissionTs = now;
                }

                producer.flush();
                Thread.sleep(RENEWABLE_INTERVAL_MS);
            }
        }
    }

    private static int sendRenewable(KafkaProducer<String, String> producer,
                                     List<RenewableOutputEvent> batch) {
        int ok = 0;
        for (RenewableOutputEvent ev : batch) {
            try {
                String json = MAPPER.writeValueAsString(ev);
                String key  = ev.getRegionCode() + ":" + ev.getSourceType();
                producer.send(new ProducerRecord<>(RENEWABLE_TOPIC, key, json),
                        (md, ex) -> { if (ex != null) log.error("send renewable fail [{}]: {}", key, ex.getMessage()); });
                ok++;
            } catch (Exception e) {
                log.error("serialize renewable fail: {}", e.getMessage());
            }
        }
        return ok;
    }

    private static int sendEmission(KafkaProducer<String, String> producer,
                                    List<EmissionEvent> batch) {
        int ok = 0;
        for (EmissionEvent ev : batch) {
            try {
                String json = MAPPER.writeValueAsString(ev);
                String key  = ev.getRegionCode();
                producer.send(new ProducerRecord<>(EMISSION_TOPIC, key, json),
                        (md, ex) -> { if (ex != null) log.error("send emission fail [{}]: {}", key, ex.getMessage()); });
                ok++;
            } catch (Exception e) {
                log.error("serialize emission fail: {}", e.getMessage());
            }
        }
        return ok;
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
