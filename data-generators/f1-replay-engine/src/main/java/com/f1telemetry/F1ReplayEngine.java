package com.f1telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1telemetry.avro.CarTelemetryEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

/**
 * F1 Telemetry Replay Engine.
 *
 * Reads TracingInsights column-oriented telemetry JSON (Abu Dhabi GP 2025 Race),
 * reconstructs absolute event-time, transposes column arrays → row events,
 * sorts cross-driver by event_time, publishes Avro to Kafka.
 *
 * VERIFIED SCHEMA (from TracingInsights/2025 repo):
 *
 * session_laptimes.json — column-oriented dict:
 *   "drv": ["ALB","ALB","VER",...]     driver code (3-letter)
 *   "lap": [1,2,1,...]                  lap number
 *   "lSD": ["2025-12-07T13:03:27.585000000",...]  lap start datetime (ISO, UTC, no 'Z')
 *   "dNum": ["23","23","1",...]         driver number (string)
 *
 * drivers.json — row-oriented:
 *   {"drivers": [{"driver":"VER","dn":"1","team":"Red Bull",...}]}
 *
 * {DRIVER}/{N}_tel.json — column-oriented:
 *   {"tel": {"time":[0.0,0.05,...], "speed":[...], "throttle":[...],
 *            "brake":[...], "rpm":[...], "gear":[...], "drs":[...]}}
 *   time[] = lap-relative seconds (resets to 0 each lap)
 *   Some arrays contain "None" (string) for missing values → must treat as null.
 *
 * EVENT-TIME RECONSTRUCTION:
 *   event_time_ms = parse(lSD[drv][lap]).toEpochMilli() + (long)(tel.time[i] * 1000)
 *
 * Run: mvn -pl data-generators/f1-replay-engine exec:java
 *   (run from repo root; dataset path is relative to working directory)
 */
public class F1ReplayEngine {

    private static final Logger LOG = LoggerFactory.getLogger(F1ReplayEngine.class);

    private static final String KAFKA_BOOTSTRAP = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    private static final String SCHEMA_REGISTRY_URL = env("SCHEMA_REGISTRY_URL", "http://localhost:8081");
    private static final String TOPIC = env("KAFKA_TOPIC", "car-telemetry-events");
    private static final int REPLAY_SPEED = Integer.parseInt(env("REPLAY_SPEED_FACTOR", "5"));
    private static final String DATASET_PATH = env("F1_DATASET_PATH", "src/main/resources/datasets");

    private final KafkaProducer<Integer, CarTelemetryEvent> producer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path datasetRoot = resolveDatasetRoot();

    public F1ReplayEngine() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 65536);
        this.producer = new KafkaProducer<>(props);
        LOG.info("Replay Engine init | Kafka={} SR={} topic={} speed={}x",
                KAFKA_BOOTSTRAP, SCHEMA_REGISTRY_URL, TOPIC, REPLAY_SPEED);
    }

    public void run() throws Exception {
        // 1. Load driver code → driver number mapping from drivers.json
        Map<String, Integer> driverNumbers = loadDriverNumbers();
        if (driverNumbers.isEmpty()) {
            throw new IllegalStateException("No drivers loaded. Run scripts/fetch_f1_session.sh first.");
        }
        LOG.info("Drivers: {}", driverNumbers);

        // 2. Load lap start times: (driverCode, lapNumber) → epochMillis
        //    Uses lSD field (absolute ISO datetime) from session_laptimes.json
        Map<String, Map<Integer, Long>> lapStarts = loadLapStartTimes();
        LOG.info("Lap start times for {} drivers", lapStarts.size());

        // 3. Read all telemetry files, transpose columns → rows, reconstruct event_time
        List<CarTelemetryEvent> events = loadAllTelemetry(driverNumbers, lapStarts);
        LOG.info("Total events loaded: {}", events.size());

        // 4. Sort by event_time (cross-driver, cross-lap — ensures watermark correctness)
        events.sort(Comparator.comparingLong(CarTelemetryEvent::getEventTime));
        LOG.info("Events sorted by event_time");

        // 5. Publish with speed-controlled replay
        publish(events);

        producer.flush();
        producer.close();
        LOG.info("Replay complete.");
    }

    // ── Load drivers.json → Map(driverCode, driverNumber) ───

    private Map<String, Integer> loadDriverNumbers() throws Exception {
        Path file = datasetRoot.resolve("drivers.json");
        if (!Files.exists(file)) {
            throw new IllegalStateException("drivers.json not found: " + file);
        }
        Map<String, Integer> result = new HashMap<>();
        JsonNode drivers = mapper.readTree(file.toFile()).get("drivers");
        if (drivers == null || !drivers.isArray()) {
            throw new IllegalStateException("drivers.json missing 'drivers' array");
        }
        for (JsonNode d : drivers) {
            result.put(d.get("driver").asText(), d.get("dn").asInt());
        }
        return result;
    }

    // ── Load session_laptimes.json → Map(driverCode, Map(lap, epochMillis)) ───
    //    Parses COLUMN-ORIENTED dict: drv[], lap[], lSD[]

    private Map<String, Map<Integer, Long>> loadLapStartTimes() throws Exception {
        Path file = datasetRoot.resolve("session_laptimes.json");
        if (!Files.exists(file)) {
            throw new IllegalStateException("session_laptimes.json not found: " + file);
        }
        JsonNode root = mapper.readTree(file.toFile());
        JsonNode drvArr = root.get("drv");
        JsonNode lapArr = root.get("lap");
        JsonNode lsdArr = root.get("lSD");

        if (drvArr == null || lapArr == null || lsdArr == null) {
            throw new IllegalStateException("session_laptimes.json missing required fields: drv, lap, lSD");
        }

        Map<String, Map<Integer, Long>> result = new HashMap<>();
        for (int i = 0; i < drvArr.size(); i++) {
            String code = drvArr.get(i).asText();
            int lap = lapArr.get(i).asInt();
            JsonNode lsd = lsdArr.get(i);

            // Skip "None" or null entries
            if (lsd.isNull() || (lsd.isTextual() && "None".equals(lsd.asText()))) continue;

            try {
                // lSD format: "2025-12-07T13:03:27.585000000" (no 'Z' suffix → append for UTC)
                long epochMs = Instant.parse(lsd.asText() + "Z").toEpochMilli();
                result.computeIfAbsent(code, k -> new HashMap<>()).put(lap, epochMs);
            } catch (Exception e) {
                LOG.warn("Cannot parse lSD '{}': {}", lsd.asText(), e.getMessage());
            }
        }
        return result;
    }

    // ── Read all {DRIVER}/{N}_tel.json, transpose, reconstruct event_time ───

    private List<CarTelemetryEvent> loadAllTelemetry(
            Map<String, Integer> driverNumbers,
            Map<String, Map<Integer, Long>> lapStarts) throws Exception {

        List<CarTelemetryEvent> events = new ArrayList<>();
        Path racePath = datasetRoot;

        if (!Files.exists(racePath)) {
            throw new IllegalStateException("Dataset path not found: " + racePath);
        }

        // Iterate driver directories (named by 3-letter code: VER, ALB, etc.)
        try (var driverDirs = Files.list(racePath)) {
            driverDirs.filter(Files::isDirectory).forEach(driverDir -> {
                String driverCode = driverDir.getFileName().toString();
                // Must be a 3-letter uppercase code present in drivers.json
                if (driverCode.length() != 3 || !driverNumbers.containsKey(driverCode)) {
                    return;
                }
                int driverNumber = driverNumbers.get(driverCode);
                Map<Integer, Long> starts = lapStarts.getOrDefault(driverCode, Collections.emptyMap());
                if (starts.isEmpty()) return;

                try {
                    File[] telFiles = driverDir.toFile()
                            .listFiles((d, n) -> n.matches("\\d+_tel\\.json"));
                    if (telFiles == null) return;

                    // Sort by lap number
                    Arrays.sort(telFiles, Comparator.comparing(
                            f -> Integer.parseInt(f.getName().split("_")[0])));

                    for (File telFile : telFiles) {
                        int lapNumber = Integer.parseInt(telFile.getName().split("_")[0]);
                        Long lapStartMs = starts.get(lapNumber);
                        if (lapStartMs == null) continue;

                        events.addAll(parseTelemetryFile(telFile, driverNumber, lapStartMs));
                    }
                } catch (Exception e) {
                    LOG.error("Error processing driver {}: {}", driverCode, e.getMessage());
                }
            });
        }
        return events;
    }

    // ── Parse one telemetry file: column-oriented → row events ───

    private List<CarTelemetryEvent> parseTelemetryFile(
            File telFile, int driverNumber, long lapStartMs) throws Exception {

        JsonNode tel = mapper.readTree(telFile).get("tel");
        if (tel == null || !tel.has("time") || !tel.has("speed")) {
            LOG.warn("Invalid telemetry file (missing tel/time/speed): {}", telFile.getName());
            return Collections.emptyList();
        }

        JsonNode times = tel.get("time");
        JsonNode speeds = tel.get("speed");
        int n = times.size();

        List<CarTelemetryEvent> events = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            // Reconstruct absolute event_time (epoch millis)
            long eventTimeMs = lapStartMs + (long) (times.get(i).asDouble() * 1000);

            CarTelemetryEvent event = CarTelemetryEvent.newBuilder()
                    .setEventTime(eventTimeMs)
                    .setDriverNumber(driverNumber)
                    .setSpeed(speeds.get(i).asDouble())
                    .setThrottle(getDoubleOrNull(tel, "throttle", i))
                    .setBrake(getDoubleOrNull(tel, "brake", i))
                    .setRpm(getIntOrNull(tel, "rpm", i))
                    .setGear(getIntOrNull(tel, "gear", i))
                    .setDrs(getIntOrNull(tel, "drs", i))
                    .build();
            events.add(event);
        }
        return events;
    }

    // ── Publish events with replay speed control ───

    private void publish(List<CarTelemetryEvent> events) throws InterruptedException {
        long replayStartWall = System.currentTimeMillis();
        long firstEventTime = events.isEmpty() ? 0 : events.get(0).getEventTime();
        int count = 0;

        for (CarTelemetryEvent event : events) {
            // Speed control: sleep proportional to event-time delta from first event
            long delta = event.getEventTime() - firstEventTime;
            long targetWall = replayStartWall + (delta / REPLAY_SPEED);
            long now = System.currentTimeMillis();
            if (now < targetWall) {
                Thread.sleep(targetWall - now);
            }

            producer.send(new ProducerRecord<>(TOPIC, event.getDriverNumber(), event),
                    (meta, ex) -> {
                        if (ex != null) LOG.error("Send failed: driver={}",
                                event.getDriverNumber(), ex);
                    });

            count++;
            if (count % 10000 == 0) {
                LOG.info("Published {} events | driver={} speed={}km/h",
                        count, event.getDriverNumber(),
                        String.format("%.0f", event.getSpeed()));
            }
        }
        LOG.info("Published {} events total", count);
    }

    // ── Nullable field helpers (handle "None" string in source data) ───

    private static Double getDoubleOrNull(JsonNode tel, String field, int i) {
        JsonNode arr = tel.get(field);
        if (arr == null || i >= arr.size()) return null;
        JsonNode v = arr.get(i);
        if (v.isNull() || v.isTextual()) return null; // "None" → null
        return v.asDouble();
    }

    private static Integer getIntOrNull(JsonNode tel, String field, int i) {
        JsonNode arr = tel.get(field);
        if (arr == null || i >= arr.size()) return null;
        JsonNode v = arr.get(i);
        if (v.isNull() || v.isTextual()) return null;
        return v.asInt();
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return v != null ? v : def;
    }

    private Path resolveDatasetRoot() {
        List<Path> candidates = List.of(
                Paths.get(DATASET_PATH),
                Paths.get("src/main/resources/datasets"),
                Paths.get("../src/main/resources/datasets"),
                Paths.get("data-generators/f1-replay-engine/src/main/resources/datasets"),
                Paths.get("../data-generators/f1-replay-engine/src/main/resources/datasets")
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }

        try {
            var resourceUrl = F1ReplayEngine.class.getClassLoader().getResource("datasets");
            if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
                return Paths.get(resourceUrl.toURI()).toAbsolutePath().normalize();
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid datasets resource URI", e);
        }

        throw new IllegalStateException("Dataset path not found. Tried: " + candidates);
    }

    public static void main(String[] args) {
        try {
            new F1ReplayEngine().run();
        } catch (Exception e) {
            LOG.error("Replay failed", e);
            System.exit(1);
        }
    }
}
