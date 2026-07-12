package vn.edu.f1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * F1 Telemetry Replay Engine.
 *
 * Reads TracingInsights column-oriented telemetry JSON (Abu Dhabi GP 2025 Race),
 * reconstructs absolute event-time from lap start times,
 * transposes column arrays into row events, sorts cross-driver by event_time,
 * and publishes Avro records to Kafka with configurable replay speed.
 *
 * Data source: https://github.com/TracingInsights/2025/tree/main/Abu%20Dhabi%20Grand%20Prix/Race
 *
 * Run: mvn -pl data-generators/f1-replay-engine exec:java
 */
public class F1ReplayEngine {

    private static final Logger LOG = LoggerFactory.getLogger(F1ReplayEngine.class);

    private static final String TOPIC = "car-telemetry-events";

    // F1 telemetry fields (column-oriented in TracingInsights JSON)
    private static final String[] TEL_FIELDS = {
        "speed", "throttle", "brake", "rpm", "gear", "drs", "x", "y", "z"
    };

    public static void main(String[] args) {
        String datasetPath = System.getenv().getOrDefault("F1_DATASET_PATH",
                "data-generators/f1-replay-engine/src/main/resources/datasets");
        int speedFactor = Integer.parseInt(System.getenv().getOrDefault("REPLAY_SPEED_FACTOR", "5"));
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String schemaRegistryUrl = System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081");

        LOG.info("=== F1 Replay Engine ===");
        LOG.info("Dataset path: {}", datasetPath);
        LOG.info("Replay speed: {}x", speedFactor);
        LOG.info("Kafka: {}", bootstrapServers);
        LOG.info("Schema Registry: {}", schemaRegistryUrl);

        try {
            ObjectMapper mapper = new ObjectMapper();

            // 1. Load lap start times from session_laptimes.json
            //    Maps (driverCode, lapNumber) -> absolute lap start time (epoch millis)
            Map<String, Map<Integer, Long>> lapStartTimes = loadLapStartTimes(
                    Path.of(datasetPath, "session_laptimes.json"), mapper);
            LOG.info("Loaded lap start times for {} drivers", lapStartTimes.size());

            // 2. Load driver code -> driver number mapping from drivers.json
            Map<String, Integer> driverNumbers = loadDriverNumbers(
                    Path.of(datasetPath, "drivers.json"), mapper);
            LOG.info("Loaded driver numbers: {}", driverNumbers);

            // 3. Read all telemetry files, transpose, and reconstruct event-time
            List<Map<String, Object>> allEvents = new ArrayList<>();
            File datasetDir = new File(datasetPath);
            File[] driverDirs = datasetDir.listFiles(File::isDirectory);

            if (driverDirs == null) {
                LOG.error("No driver directories found in {}", datasetPath);
                return;
            }

            for (File driverDir : driverDirs) {
                String driverCode = driverDir.getName();
                if (!driverNumbers.containsKey(driverCode)) {
                    LOG.debug("Skipping unknown driver directory: {}", driverCode);
                    continue;
                }
                int driverNumber = driverNumbers.get(driverCode);

                // Get lap start times for this driver
                Map<Integer, Long> driverLapStarts = lapStartTimes.getOrDefault(driverCode, new HashMap<>());
                if (driverLapStarts.isEmpty()) {
                    LOG.warn("No lap start times for driver {}, skipping", driverCode);
                    continue;
                }

                // Process each lap telemetry file
                File[] lapFiles = driverDir.listFiles((dir, name) ->
                        name.matches("\\d+_tel\\.json"));
                if (lapFiles == null) continue;

                for (File lapFile : lapFiles) {
                    // Extract lap number from filename: "1_tel.json" -> lap 1
                    String lapNumStr = lapFile.getName().split("_")[0];
                    int lapNumber;
                    try {
                        lapNumber = Integer.parseInt(lapNumStr);
                    } catch (NumberFormatException e) {
                        LOG.warn("Cannot parse lap number from: {}", lapFile.getName());
                        continue;
                    }

                    Long lapStartEpoch = driverLapStarts.get(lapNumber);
                    if (lapStartEpoch == null) {
                        LOG.warn("No lap start time for {} lap {}, skipping", driverCode, lapNumber);
                        continue;
                    }

                    // Parse column-oriented JSON: {"tel": {"time": [0.0, 0.05, ...], "speed": [...], ...}}
                    JsonNode root = mapper.readTree(lapFile);
                    JsonNode tel = root.get("tel");
                    if (tel == null || !tel.has("time") || !tel.has("speed")) {
                        LOG.warn("Invalid telemetry file (missing tel/time/speed): {}", lapFile.getName());
                        continue;
                    }

                    JsonNode timeArray = tel.get("time");
                    JsonNode speedArray = tel.get("speed");
                    int sampleCount = timeArray.size();

                    // Transpose: column arrays -> row events
                    for (int i = 0; i < sampleCount; i++) {
                        double lapRelativeTime = timeArray.get(i).asDouble();
                        double speed = speedArray.get(i).asDouble();

                        // Reconstruct absolute event_time (epoch millis)
                        // lapStartEpoch is in epoch seconds -> convert to millis, add lap-relative seconds
                        long eventTimeMs = (lapStartEpoch * 1000) + (long) (lapRelativeTime * 1000);

                        Map<String, Object> event = new HashMap<>();
                        event.put("event_time", eventTimeMs);
                        event.put("driver_number", driverNumber);
                        event.put("speed", speed);

                        // Optional fields (nullable)
                        event.put("throttle", getDoubleOrNull(tel, "throttle", i));
                        event.put("brake", getDoubleOrNull(tel, "brake", i));
                        event.put("rpm", getIntOrNull(tel, "rpm", i));
                        event.put("gear", getIntOrNull(tel, "gear", i));
                        event.put("drs", getIntOrNull(tel, "drs", i));

                        allEvents.add(event);
                    }
                }
            }

            LOG.info("Total events loaded: {}", allEvents.size());

            // 4. Sort all events by event_time (cross-driver, cross-lap ordering)
            allEvents.sort(Comparator.comparingLong(e -> (Long) e.get("event_time")));
            LOG.info("Events sorted by event_time");

            // 5. Publish to Kafka as Avro
            publishEvents(allEvents, bootstrapServers, schemaRegistryUrl, speedFactor);

        } catch (IOException e) {
            LOG.error("Fatal error in replay engine", e);
            System.exit(1);
        }
    }

    /**
     * Load session_laptimes.json -> Map(driverCode, Map(lapNumber, lapStartEpochSeconds))
     *
     * Expected format from TracingInsights:
     * Array of objects with driver_number (int), lap_duration (float), date_start (ISO timestamp)
     */
    private static Map<String, Map<Integer, Long>> loadLapStartTimes(Path file, ObjectMapper mapper)
            throws IOException {
        Map<String, Map<Integer, Long>> result = new HashMap<>();

        if (!Files.exists(file)) {
            LOG.warn("session_laptimes.json not found at {}, using fallback", file);
            return result;
        }

        JsonNode root = mapper.readTree(file);
        // The file may be an array of lap entries or a dict
        Iterator<JsonNode> elements = root.isArray() ? root.elements() : root.get("lap_data").elements();

        // We need to derive lap start times from lap durations.
        // If the file contains cumulative data, parse accordingly.
        // Fallback: assign approximate start times based on lap order + typical F1 lap (~90s).
        // This will be refined once we inspect the actual file structure.

        // TODO: Parse actual session_laptimes.json structure from TracingInsights
        // For now, return empty — will be populated when data is fetched

        return result;
    }

    /**
     * Load drivers.json -> Map(driverCode, driverNumber)
     *
     * Expected format: {"drivers": [{"driver": "VER", "dn": "1", ...}, ...]}
     */
    private static Map<String, Integer> loadDriverNumbers(Path file, ObjectMapper mapper)
            throws IOException {
        Map<String, Integer> result = new HashMap<>();

        if (!Files.exists(file)) {
            LOG.warn("drivers.json not found at {}", file);
            return result;
        }

        JsonNode root = mapper.readTree(file);
        JsonNode drivers = root.get("drivers");
        if (drivers == null || !drivers.isArray()) {
            LOG.warn("drivers.json missing 'drivers' array");
            return result;
        }

        for (JsonNode driver : drivers) {
            String code = driver.get("driver").asText();
            int number = driver.get("dn").asInt();
            result.put(code, number);
        }

        return result;
    }

    private static void publishEvents(List<Map<String, Object>> events,
                                       String bootstrapServers, String schemaRegistryUrl,
                                       int speedFactor) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 50);

        try (KafkaProducer<Integer, GenericRecord> producer = new KafkaProducer<>(props)) {

            long prevEventTime = -1;
            int count = 0;

            for (Map<String, Object> event : events) {
                long eventTime = (Long) event.get("event_time");
                int driverNumber = (Integer) event.get("driver_number");
                double speed = (Double) event.get("speed");

                // Build Avro GenericRecord matching car-telemetry-event.avsc schema
                // The KafkaAvroSerializer will use Schema Registry to serialize
                GenericRecord record = buildAvroRecord(event);

                ProducerRecord<Integer, GenericRecord> record_ =
                        new ProducerRecord<>(TOPIC, driverNumber, record);

                producer.send(record_, (metadata, exception) -> {
                    if (exception != null) {
                        LOG.error("Failed to send event: driver={}, time={}", driverNumber, eventTime, exception);
                    }
                });

                // Sleep to simulate replay speed
                if (prevEventTime >= 0) {
                    long realDeltaMs = eventTime - prevEventTime;
                    long sleepMs = realDeltaMs / speedFactor;
                    if (sleepMs > 0) {
                        Thread.sleep(sleepMs);
                    }
                }

                prevEventTime = eventTime;
                count++;

                if (count % 10000 == 0) {
                    LOG.info("Published {} events (speed={}km/h, driver={}, time={})",
                            count, String.format("%.0f", speed), driverNumber,
                            Instant.ofEpochMilli(eventTime).atZone(ZoneOffset.UTC));
                }
            }

            producer.flush();
            LOG.info("Replay complete. Total events published: {}", count);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Replay interrupted");
        }
    }

    private static GenericRecord buildAvroRecord(Map<String, Object> event) {
        // Build using SpecificRecord or GenericRecord
        // For now, we'll use the generated CarTelemetryEvent class (Avro codegen)
        // This method will be updated in Phase 4 implementation
        // Using GenericRecord as fallback

        // TODO Phase 4: replace with vn.edu.f1.avro.CarTelemetryEvent.newBuilder()...build()
        return null; // placeholder — will be replaced with actual Avro record building
    }

    private static Double getDoubleOrNull(JsonNode tel, String field, int index) {
        JsonNode arr = tel.get(field);
        if (arr == null || index >= arr.size()) return null;
        JsonNode val = arr.get(index);
        if (val.isNull()) return null;
        return val.asDouble();
    }

    private static Integer getIntOrNull(JsonNode tel, String field, int index) {
        JsonNode arr = tel.get(field);
        if (arr == null || index >= arr.size()) return null;
        JsonNode val = arr.get(index);
        if (val.isNull()) return null;
        return val.asInt();
    }
}
