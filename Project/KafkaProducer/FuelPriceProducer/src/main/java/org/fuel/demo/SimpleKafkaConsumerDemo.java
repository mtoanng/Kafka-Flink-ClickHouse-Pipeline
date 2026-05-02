package org.fuel.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;

/**
 * ╔════════════════════════════════════════════════════════════════╗
 * ║        KAFKA SEMINAR DEMO — SimpleKafkaConsumerDemo           ║
 * ╠════════════════════════════════════════════════════════════════╣
 * ║  Mục đích: Demo cách Kafka Consumer hoạt động cho seminar.    ║
 * ║  Không dùng Flink — chỉ thuần Kafka Consumer API.            ║
 * ║                                                                ║
 * ║  Luồng:                                                        ║
 * ║    [Producer] ──► [Kafka Topic: fuel-prices] ──► [Consumer]  ║
 * ║                                                                ║
 * ║  Chạy song song với FuelPriceProducer để thấy real-time.      ║
 * ╚════════════════════════════════════════════════════════════════╝
 *
 * Cách chạy (xem kafka-seminar-demo.md để có script đầy đủ):
 *   mvn exec:java -Dexec.mainClass="org.fuel.demo.SimpleKafkaConsumerDemo"
 */
public class SimpleKafkaConsumerDemo {

    // ── Màu ANSI cho output đẹp trên terminal ──────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";
    private static final String BLUE   = "\u001B[34m";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── Cấu hình — đổi thành địa chỉ Kafka thực nếu cần ───────────────────
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC             = "fuel-prices";
    // Consumer Group ID — quan trọng: nhiều consumer cùng group sẽ chia sẻ partitions
    private static final String GROUP_ID          = "seminar-demo-group";

    public static void main(String[] args) throws InterruptedException {
        printBanner();

        // ══════════════════════════════════════════════════════════════════
        // BƯỚC 1: CẤU HÌNH CONSUMER
        //   - bootstrap.servers : địa chỉ Kafka broker
        //   - group.id          : Consumer Group (offset tracking theo group)
        //   - auto.offset.reset : "earliest" = đọc từ đầu topic nếu chưa có offset
        //   - enable.auto.commit: false = tự quản lý commit (safer)
        // ══════════════════════════════════════════════════════════════════
        Properties props = buildConsumerConfig();
        printStep(1, "Tạo cấu hình Consumer", props);

        // ══════════════════════════════════════════════════════════════════
        // BƯỚC 2: KHỞI TẠO KAFKA CONSUMER & SUBSCRIBE TOPIC
        // ══════════════════════════════════════════════════════════════════
        printStep(2, "Khởi tạo KafkaConsumer & subscribe topic [" + TOPIC + "]", null);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            // Subscribe vào topic — Kafka sẽ tự gán partition cho consumer này
            consumer.subscribe(Collections.singletonList(TOPIC));

            printInfo("Consumer đã subscribe. Đang chờ messages...");
            printInfo("(Hãy chạy FuelPriceProducer ở terminal khác!)");
            printDivider();

            int totalReceived = 0;

            // ══════════════════════════════════════════════════════════════
            // BƯỚC 3: POLL LOOP — vòng lặp chính của mọi Kafka Consumer
            //   consumer.poll(timeout) : kéo messages từ broker
            //   - timeout : thời gian chờ tối đa nếu không có message
            //   - Kafka KHÔNG push về consumer, consumer tự PULL (pull model)
            // ══════════════════════════════════════════════════════════════
            printStep(3, "Bắt đầu Poll Loop (Pull Model — consumer chủ động kéo dữ liệu)", null);

            while (true) {
                // poll() kéo batch messages từ Kafka broker (tối đa 500ms chờ)
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

                if (!records.isEmpty()) {
                    System.out.printf("%n%s[POLL] Nhận được %d message(s) từ Kafka%s%n",
                            GREEN, records.count(), RESET);

                    for (ConsumerRecord<String, String> record : records) {
                        totalReceived++;
                        printRecord(record, totalReceived);
                    }

                    // ══════════════════════════════════════════════════════
                    // BƯỚC 4: COMMIT OFFSET
                    //   Kafka lưu "offset" để biết consumer đọc đến đâu.
                    //   commitSync() = commit đồng bộ, chắc chắn không mất message.
                    //   commitAsync() = commit bất đồng bộ, throughput cao hơn.
                    // ══════════════════════════════════════════════════════
                    consumer.commitSync();
                    System.out.printf("  %s✓ Offset đã commit (consumer sẽ tiếp tục từ đây nếu restart)%s%n",
                            CYAN, RESET);
                }

                // Graceful shutdown nếu nhấn Ctrl+C
                if (Thread.currentThread().isInterrupted()) break;
            }
        }
    }

    // ── Hiển thị một ConsumerRecord theo format đẹp cho seminar ────────────
    private static void printRecord(ConsumerRecord<String, String> record, int count) {
        String time = LocalTime.now().format(TIME_FMT);

        System.out.printf("%n  %s┌─ Message #%d ─────────────────────────────────────────%s%n",
                BOLD, count, RESET);
        System.out.printf("  %s│%s %sTopic:%s     %s%n", BOLD, RESET, YELLOW, RESET, record.topic());
        System.out.printf("  %s│%s %sPartition:%s %d    %sOffset:%s %d%n",
                BOLD, RESET, YELLOW, RESET, record.partition(),
                YELLOW, RESET, record.offset());
        System.out.printf("  %s│%s %sKey:%s       %s%s%s%n",
                BOLD, RESET, YELLOW, RESET, CYAN, record.key(), RESET);
        System.out.printf("  %s│%s %sReceived:%s  %s%n", BOLD, RESET, YELLOW, RESET, time);

        // Parse JSON để hiển thị giá trực quan
        try {
            JsonNode json = MAPPER.readTree(record.value());
            String fuelType = json.path("fuel_type").asText("?");
            double price    = json.path("price").asDouble(0);
            String unit     = json.path("price_unit").asText("?");
            String location = json.path("location").asText("?");

            System.out.printf("  %s│%s %sPayload:%s   %s%-20s%s → %s%.4f %s%s @ %s%n",
                    BOLD, RESET, YELLOW, RESET,
                    GREEN, fuelType, RESET,
                    BOLD, price, unit, RESET,
                    location);
        } catch (Exception e) {
            // Nếu JSON không parse được, hiện raw
            System.out.printf("  %s│%s Payload (raw): %s%n", BOLD, RESET, record.value());
        }

        System.out.printf("  %s└──────────────────────────────────────────────────────%s%n",
                BOLD, RESET);
    }

    // ── Build cấu hình Kafka Consumer ──────────────────────────────────────
    private static Properties buildConsumerConfig() {
        Properties props = new Properties();

        // [DEMO KEY 1] Kafka broker address — điểm kết nối duy nhất cần biết
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // [DEMO KEY 2] Consumer Group — cơ chế scale-out và offset tracking
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);

        // [DEMO KEY 3] Deserializer — ngược với Serializer của Producer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // [DEMO KEY 4] auto.offset.reset — hành vi khi chưa có offset cho group này
        //   "earliest" = đọc từ đầu (thấy hết message cũ — tốt cho demo)
        //   "latest"   = chỉ đọc message mới (production thường dùng)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // [DEMO KEY 5] Tắt auto-commit để demo commit thủ công
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // Tối đa 50 messages mỗi poll — giảm cho demo dễ đọc
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "50");

        return props;
    }

    // ── Helpers hiển thị ───────────────────────────────────────────────────
    private static void printBanner() {
        System.out.println(BOLD + BLUE);
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         KAFKA DEMO — Simple Consumer (Plain Java)           ║");
        System.out.println("║   Hiển thị messages real-time từ topic: fuel-prices         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println(RESET);
        System.out.println(CYAN + "  Kafka Pull Model:" + RESET);
        System.out.println("  Producer ──► [Kafka Broker] ◄── Consumer (chủ động poll)");
        System.out.println();
    }

    private static void printStep(int step, String desc, Properties props) {
        System.out.printf("%n%s═══ BƯỚC %d: %s%s%n", BOLD + YELLOW, step, desc, RESET);
        if (props != null) {
            props.forEach((k, v) -> System.out.printf("     %s%-35s%s = %s%s%s%n",
                    CYAN, k, RESET, GREEN, v, RESET));
        }
    }

    private static void printInfo(String msg) {
        System.out.printf("  %s▶%s %s%n", GREEN, RESET, msg);
    }

    private static void printDivider() {
        System.out.println(BLUE + "  ──────────────────────────────────────────────────────" + RESET);
    }
}
