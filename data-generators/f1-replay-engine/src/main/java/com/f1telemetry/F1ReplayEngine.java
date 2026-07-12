package com.f1telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.f1telemetry.avro.CarTelemetryEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * F1 Replay Engine
 * Reads TracingInsights telemetry data (Abu Dhabi GP 2025 Race),
 * reconstructs absolute event timestamps, and publishes to Kafka.
 */
public class F1ReplayEngine {
    private static final Logger LOG = LoggerFactory.getLogger(F1ReplayEngine.class);
    
    // Config from env or defaults
    private static final String KAFKA_BOOTSTRAP = System.getenv().getOrDefault(
            "KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    private static final String SCHEMA_REGISTRY_URL = System.getenv().getOrDefault(
            "SCHEMA_REGISTRY_URL", "http://localhost:8081");
    private static final String TOPIC = System.getenv().getOrDefault(
            "KAFKA_TOPIC", "car-telemetry-events");
    private static final int REPLAY_SPEED_FACTOR = Integer.parseInt(
            System.getenv().getOrDefault("REPLAY_SPEED_FACTOR", "5"));
    
    private static final String DATASET_PATH = "src/main/resources/datasets/Abu Dhabi Grand Prix/Race";
    
    private final KafkaProducer<Integer, CarTelemetryEvent> producer;
    private final ObjectMapper mapper = new ObjectMapper();
    
    public F1ReplayEngine() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        this.producer = new KafkaProducer<>(props);
        LOG.info("F1 Replay Engine initialized. Kafka={}, Registry={}, Topic={}, SpeedFactor={}x",
                KAFKA_BOOTSTRAP, SCHEMA_REGISTRY_URL, TOPIC, REPLAY_SPEED_FACTOR);
    }
    
    public void run() throws IOException, InterruptedException {
        // Step 1: Load lap start times from session_laptimes.json
        Map<Integer, Map<Integer, Long>> lapStartTimes = loadLapStartTimes();
        LOG.info("Loaded lap start times for {} drivers", lapStartTimes.size());
        
        // Step 2: Load all telemetry files and reconstruct event_time
        List<TelemetryEvent> events = loadAllTelemetry(lapStartTimes);
        LOG.info("Loaded {} telemetry events from dataset", events.size());
        
        // Step 3: Global sort by event_time (ensures watermark correctness)
        events.sort(Comparator.comparingLong(e -> e.eventTime));
        LOG.info("Events sorted by event_time");
        
        // Step 4: Replay events with configurable speed factor
        replayEvents(events);
        
        producer.flush();
        producer.close();
        LOG.info("Replay completed. Producer closed.");
    }
    
    private Map<Integer, Map<Integer, Long>> loadLapStartTimes() throws IOException {
        Path laptimesPath = Paths.get(DATASET_PATH, "session_laptimes.json");
        if (!Files.exists(laptimesPath)) {
            throw new IOException("session_laptimes.json not found. Run scripts/fetch_f1_session.sh first.");
        }
        
        JsonNode root = mapper.readTree(laptimesPath.toFile());
        Map<Integer, Map<Integer, Long>> result = new HashMap<>();
        
        // Parse structure: { "1": { "1": "2025-12-08T14:00:05.123Z", "2": "..." }, ... }
        root.fields().forEachRemaining(driverEntry -> {
            int driverNumber = Integer.parseInt(driverEntry.getKey());
            Map<Integer, Long> laps = new HashMap<>();
            
            driverEntry.getValue().fields().forEachRemaining(lapEntry -> {
                int lapNumber = Integer.parseInt(lapEntry.getKey());
                String isoTimestamp = lapEntry.getValue().asText();
                // Convert ISO timestamp to epoch millis
                long epochMillis = parseIsoTimestamp(isoTimestamp);
                laps.put(lapNumber, epochMillis);
            });
            
            result.put(driverNumber, laps);
        });
        
        return result;
    }
    
    private List<TelemetryEvent> loadAllTelemetry(Map<Integer, Map<Integer, Long>> lapStartTimes) throws IOException {
        List<TelemetryEvent> events = new ArrayList<>();
        Path racePath = Paths.get(DATASET_PATH);
        
        if (!Files.exists(racePath)) {
            throw new IOException("Dataset path not found: " + DATASET_PATH + 
                    ". Run scripts/fetch_f1_session.sh to download data.");
        }
        
        // Iterate through driver directories
        try (var driverDirs = Files.list(racePath)) {
            driverDirs.filter(Files::isDirectory).forEach(driverDir -> {
                try {
                    int driverNumber = extractDriverNumber(driverDir.getFileName().toString());
                    if (driverNumber == -1 || !lapStartTimes.containsKey(driverNumber)) return;
                    
                    // Load all *_tel.json files
                    try (var telFiles = Files.list(driverDir)) {
                        telFiles.filter(p -> p.getFileName().toString().endsWith("_tel.json"))
                                .forEach(telFile -> {
                                    try {
                                        int lapNumber = extractLapNumber(telFile.getFileName().toString());
                                        if (lapNumber == -1) return;
                                        
                                        Long lapStartTime = lapStartTimes.get(driverNumber).get(lapNumber);
                                        if (lapStartTime == null) {
                                            LOG.warn("No lap start time for driver {} lap {}", driverNumber, lapNumber);
                                            return;
                                        }
                                        
                                        List<TelemetryEvent> lapEvents = parseTelemetryFile(
                                                telFile, driverNumber, lapStartTime);
                                        events.addAll(lapEvents);
                                        
                                    } catch (IOException e) {
                                        LOG.error("Failed to parse {}: {}", telFile, e.getMessage());
                                    }
                                });
                    }
                } catch (IOException e) {
                    LOG.error("Failed to list telemetry files for {}: {}", driverDir, e.getMessage());
                }
            });
        }
        
        return events;
    }
    
    private List<TelemetryEvent> parseTelemetryFile(Path telFile, int driverNumber, long lapStartTime) 
            throws IOException {
        JsonNode root = mapper.readTree(telFile.toFile());
        JsonNode tel = root.get("tel");
        if (tel == null || !tel.isObject()) {
            return Collections.emptyList();
        }
        
        // Column arrays: time[], speed[], throttle[], brake[], rpm[], gear[], drs[]
        List<Double> times = jsonArrayToDoubleList(tel.get("time"));
        List<Double> speeds = jsonArrayToDoubleList(tel.get("speed"));
        List<Double> throttles = jsonArrayToDoubleList(tel.get("throttle"));
        List<Double> brakes = jsonArrayToDoubleList(tel.get("brake"));
        List<Integer> rpms = jsonArrayToIntList(tel.get("rpm"));
        List<Integer> gears = jsonArrayToIntList(tel.get("gear"));
        List<Integer> drss = jsonArrayToIntList(tel.get("drs"));
        
        int size = times.size();
        List<TelemetryEvent> events = new ArrayList<>(size);
        
        // Transpose to row-oriented
        for (int i = 0; i < size; i++) {
            long eventTime = lapStartTime + (long) (times.get(i) * 1000); // lap-relative sec → absolute millis
            double speed = speeds.size() > i ? speeds.get(i) : 0.0;
            Double throttle = throttles.size() > i ? throttles.get(i) : null;
            Double brake = brakes.size() > i ? brakes.get(i) : null;
            Integer rpm = rpms.size() > i ? rpms.get(i) : null;
            Integer gear = gears.size() > i ? gears.get(i) : null;
            Integer drs = drss.size() > i ? drss.get(i) : null;
            
            events.add(new TelemetryEvent(eventTime, driverNumber, speed, throttle, brake, rpm, gear, drs));
        }
        
        return events;
    }
    
    private void replayEvents(List<TelemetryEvent> events) throws InterruptedException {
        long replayStartTime = System.currentTimeMillis();
        long firstEventTime = events.isEmpty() ? 0 : events.get(0).eventTime;
        
        int count = 0;
        for (TelemetryEvent event : events) {
            // Calculate sleep based on delta from first event
            long delta = event.eventTime - firstEventTime;
            long sleepMs = delta / REPLAY_SPEED_FACTOR;
            long targetTime = replayStartTime + sleepMs;
            long now = System.currentTimeMillis();
            
            if (now < targetTime) {
                Thread.sleep(targetTime - now);
            }
            
            // Build Avro record
            CarTelemetryEvent avroEvent = CarTelemetryEvent.newBuilder()
                    .setEventTime(event.eventTime)
                    .setDriverNumber(event.driverNumber)
                    .setSpeed(event.speed)
                    .setThrottle(event.throttle)
                    .setBrake(event.brake)
                    .setRpm(event.rpm)
                    .setGear(event.gear)
                    .setDrs(event.drs)
                    .build();
            
            producer.send(new ProducerRecord<>(TOPIC, event.driverNumber, avroEvent));
            
            count++;
            if (count % 1000 == 0) {
                LOG.info("Published {} events", count);
            }
        }
        
        LOG.info("All {} events published", count);
    }
    
    // ============ Helper methods ============
    
    private long parseIsoTimestamp(String iso) {
        // Simple ISO-8601 parser (replace with proper library if needed)
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            LOG.error("Failed to parse timestamp: {}", iso);
            return 0L;
        }
    }
    
    private int extractDriverNumber(String dirname) {
        // E.g., "VER" → try lookup, or parse from filename
        // For simplicity, assume dataset has numeric driver dirs like "1", "11", "44"
        try {
            return Integer.parseInt(dirname);
        } catch (NumberFormatException e) {
            // If not numeric, try driver code mapping (simplified)
            return -1;
        }
    }
    
    private int extractLapNumber(String filename) {
        // E.g., "12_tel.json" → 12
        try {
            return Integer.parseInt(filename.split("_")[0]);
        } catch (Exception e) {
            return -1;
        }
    }
    
    private List<Double> jsonArrayToDoubleList(JsonNode arr) {
        if (arr == null || !arr.isArray()) return Collections.emptyList();
        List<Double> result = new ArrayList<>();
        arr.forEach(node -> result.add(node.asDouble()));
        return result;
    }
    
    private List<Integer> jsonArrayToIntList(JsonNode arr) {
        if (arr == null || !arr.isArray()) return Collections.emptyList();
        List<Integer> result = new ArrayList<>();
        arr.forEach(node -> result.add(node.asInt()));
        return result;
    }
    
    // ============ Data classes ============
    
    private static class TelemetryEvent {
        final long eventTime;
        final int driverNumber;
        final double speed;
        final Double throttle, brake;
        final Integer rpm, gear, drs;
        
        TelemetryEvent(long eventTime, int driverNumber, double speed, 
                       Double throttle, Double brake, Integer rpm, Integer gear, Integer drs) {
            this.eventTime = eventTime;
            this.driverNumber = driverNumber;
            this.speed = speed;
            this.throttle = throttle;
            this.brake = brake;
            this.rpm = rpm;
            this.gear = gear;
            this.drs = drs;
        }
    }
    
    // ============ Main ============
    
    public static void main(String[] args) {
        try {
            F1ReplayEngine engine = new F1ReplayEngine();
            engine.run();
        } catch (Exception e) {
            LOG.error("Replay failed", e);
            System.exit(1);
        }
    }
}
