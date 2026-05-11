# 🎤 KAFKA SEMINAR — Script Demo Kỹ Thuật

> **Đối tượng:** Người nghe biết lập trình nhưng chưa biết Kafka.
> **Thời gian demo:** ~15–20 phút.
> **Cần:** 3 cửa sổ terminal mở sẵn.

---

## 🗺️ Kiến trúc demo sẽ chạy hôm nay

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                       │
│  Terminal 1          Terminal 2 (Kafka Broker)    Terminal 3          │
│  ┌──────────┐        ┌─────────────────────┐     ┌──────────────┐   │
│  │ Producer │──────► │  Kafka Topic         │────►│  Consumer   │   │
│  │  (Java)  │  send  │  "fuel-prices"       │ poll│  (Java)     │   │
│  │          │        │  [partition 0]       │     │             │   │
│  │  30 msg  │        │  offset: 0,1,2,...   │     │  print msg  │   │
│  │ /10 giây │        └─────────────────────┘     └──────────────┘   │
│  └──────────┘                                                         │
│                                                                       │
│  (Optional) Terminal 4: Kafka CLI tools — kafka-console-consumer     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📋 PHẦN 1 — Kafka là gì? (Nói trước khi chạy demo)

### Talking points (5 phút):

**"Kafka là một distributed message queue / event streaming platform."**

Dùng khi nào?
- Hệ thống cần xử lý dữ liệu real-time (log, sensor, giá chứng khoán…)
- Cần tách rời (decouple) Producer và Consumer
- Cần nhiều Consumer đọc cùng 1 luồng dữ liệu

**3 khái niệm cốt lõi:**

| Khái niệm      | Giải thích đơn giản                                 | Ví dụ trong demo           |
|----------------|------------------------------------------------------|----------------------------|
| **Topic**      | "Kênh" chứa messages, giống folder                  | `fuel-prices`              |
| **Producer**   | Bên GỬI messages vào Topic                          | `FuelPriceProducer.java`   |
| **Consumer**   | Bên ĐỌC messages từ Topic                           | `SimpleKafkaConsumerDemo`  |
| **Offset**     | Số thứ tự của message trong partition               | offset: 0, 1, 2, 3…        |
| **Consumer Group** | Nhóm consumers chia sẻ partition, scale out    | `seminar-demo-group`       |

---

## 🚀 PHẦN 2 — Khởi động hạ tầng

### Bước 2.1 — Start Docker (Kafka + Zookeeper)

```bash
# Chuyển vào thư mục Project
cd Project

# Chỉ start Kafka và Zookeeper (không cần Flink/Postgres cho demo cơ bản)
docker-compose up -d zookeeper kafka

# Kiểm tra container đã chạy
docker-compose ps
```

**Chờ ~15 giây** cho Kafka ready, sau đó kiểm tra:

```bash
# Xem log Kafka — tìm dòng "started (kafka.server.KafkaServer)"
docker logs kafka --tail 20
```

✅ Talking point: _"Zookeeper quản lý metadata của Kafka cluster. Kafka mới (v3.x+) đang dần bỏ Zookeeper, thay bằng KRaft mode. Nhưng đây vẫn là setup phổ biến."_

---

## 🏭 PHẦN 3 — Demo Producer

### Bước 3.1 — Build Producer

```bash
cd Project/KafkaProducer/FuelPriceProducer
mvn clean package -q
```

### Bước 3.2 — Chạy Producer (Terminal 1)

```bash
# Chạy với interval 3 giây (nhanh hơn để demo sinh động)
java -Dproducer.interval.ms=3000 \
     -jar target/FuelPriceProducer-1.0.0.jar
```

### Bước 3.3 — Xem topic đã tồn tại chưa

```bash
# Liệt kê topics trong Kafka broker
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Topic `fuel-prices` sẽ tự tạo khi Producer gửi message.

```bash
# Sau khi Producer chạy — xem chi tiết topic
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe --topic fuel-prices
```

**Kết quả mong đợi:**
```
=== Fuel Price Producer khởi động ===
Kafka: localhost:9092 | Topic: fuel-prices | Interval: 3000ms
[Batch #1] Gửi 30/30 records | topic=fuel-prices
[Batch #2] Gửi 30/30 records | topic=fuel-prices
...
```

✅ Talking point: _"Producer gửi key=`WTI_CRUDE:New York` → Kafka hash key này để chọn partition. Cùng key luôn vào cùng partition → đảm bảo ordering."_

### Bước 3.3 — Minh họa code Producer (chiếu lên màn hình)

```java
// FuelPriceProducer.java — 3 dòng cốt lõi
String json = MAPPER.writeValueAsString(price);          // Serialize → JSON
String key  = price.getFuelType() + ":" + price.getLocation(); // Partition key
ProducerRecord<String, String> record =
    new ProducerRecord<>(TOPIC_NAME, key, json);         // Tạo record
producer.send(record, callback);                          // Gửi async
```

---

## 📡 PHẦN 4 — Demo Consumer (Kafka thuần)

### Bước 4.1 — Chạy SimpleKafkaConsumerDemo (Terminal 2)

```bash
cd Project/KafkaProducer/FuelPriceProducer
mvn exec:java -Dexec.mainClass="org.fuel.demo.SimpleKafkaConsumerDemo" -q
```

**Kết quả mong đợi (màu đẹp trên terminal):**
```
╔══════════════════════════════════════════════════════════════╗
║         KAFKA DEMO — Simple Consumer (Plain Java)           ║
╚══════════════════════════════════════════════════════════════╝

═══ BƯỚC 1: Tạo cấu hình Consumer
     bootstrap.servers  = localhost:9092
     group.id           = seminar-demo-group
     auto.offset.reset  = earliest
     ...

═══ BƯỚC 2: Khởi tạo KafkaConsumer & subscribe topic [fuel-prices]
  ▶ Consumer đã subscribe. Đang chờ messages...

[POLL] Nhận được 30 message(s) từ Kafka

  ┌─ Message #1 ─────────────────────────────────────────
  │ Topic:     fuel-prices
  │ Partition: 0    Offset: 0
  │ Key:       WTI_CRUDE:New York (NYMEX)
  │ Received:  10:30:15
  │ Payload:   WTI_CRUDE            → 76.1234 USD/barrel @ New York (NYMEX)
  └──────────────────────────────────────────────────────
  ✓ Offset đã commit
```

✅ Talking points khi demo:

1. **"Chú ý Consumer đọc từ `earliest` — nó thấy cả messages cũ từ lúc Producer bắt đầu."**
2. **"Mỗi message có `Topic`, `Partition`, `Offset` — bộ 3 này xác định vị trí duy nhất."**
3. **"Consumer chủ động POLL (kéo) — Kafka không push. Đây gọi là Pull Model."**
4. **"Sau khi xử lý xong, Consumer commit offset → báo Kafka đã đọc đến đây."**

---

## 🔬 PHẦN 5 — Demo Kafka CLI (nâng cao)

### Xem messages trực tiếp bằng Kafka CLI

```bash
# Terminal 3 — dùng Kafka built-in consumer để xem raw message
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic fuel-prices \
  --from-beginning \
  --max-messages 5
```

```bash
# Xem với cả Key
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic fuel-prices \
  --property print.key=true \
  --property key.separator=" → " \
  --from-beginning \
  --max-messages 10
```

✅ Talking point: _"Đây là cách debug nhanh nhất. Mọi data trong Kafka đều dạng bytes, default CLI hiểu là String."_

---

### Xem Consumer Group & Offset

```bash
# Liệt kê tất cả consumer groups
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 --list

# Xem lag của group demo
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group seminar-demo-group
```

**Output mẫu:**
```
GROUP               TOPIC        PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
seminar-demo-group  fuel-prices  0          90              90              0
```

✅ Talking point:
- **CURRENT-OFFSET**: Consumer đã đọc đến message nào.
- **LOG-END-OFFSET**: Tổng số messages trong partition.
- **LAG = 0**: Consumer theo kịp Producer, không bị trễ.
- **LAG > 0**: Consumer đang lag — cần scale out (thêm consumer vào group).

---

## ❓ Q&A Chuẩn bị sẵn

| Câu hỏi thường gặp                         | Câu trả lời ngắn                                                                 |
|--------------------------------------------|----------------------------------------------------------------------------------|
| Kafka vs RabbitMQ?                         | Kafka lưu message lâu dài (log-based), nhiều consumer đọc lại được. RabbitMQ xóa sau khi consumer đọc xong. |
| Kafka vs Database Queue?                   | Kafka throughput triệu msg/s, horizontal scale dễ. DB queue chậm hơn nhiều.    |
| Tại sao cần Zookeeper?                     | Quản lý leader election, cluster metadata. Kafka 3.x+ thay bằng KRaft (built-in). |
| Consumer bị crash thì mất message không?  | Không, nếu commit offset sau khi xử lý xong. Consumer mới đọc lại từ offset cuối. |
| Kafka có đảm bảo thứ tự không?            | Có, trong cùng 1 partition. Giữa các partitions thì không.                       |
| Retention bao lâu?                         | Mặc định 7 ngày, cấu hình được. Kafka giữ message dù đã đọc.                   |

---

## 📌 Tóm tắt Architecture demo hôm nay

```
                    ┌─────── KAFKA CLUSTER ────────┐
                    │                               │
FuelPriceProducer   │   Topic: fuel-prices          │   SimpleKafkaConsumerDemo
(Java Producer)  ──►│   ┌──────────────────────┐   │──► (Java Consumer)
  30 msg/batch      │   │ Partition 0           │   │    poll → print → commit
  key=type:location │   │ [0][1][2][3][4]...   │   │
  acks=all          │   └──────────────────────┘   │   (Optional) Flink Consumer
                    │                               │──► aggregate → PostgreSQL
                    └───────────────────────────────┘
```

**Kafka đảm bảo:**
- ✅ **Durability**: Messages lưu trên disk, không mất khi restart
- ✅ **Scalability**: Thêm partition = thêm throughput
- ✅ **Replayability**: Consumer đọc lại bất kỳ lúc nào (trong retention period)
- ✅ **Decoupling**: Producer & Consumer hoàn toàn độc lập

---

*File tạo bởi GitHub Copilot | Project: Fuel Price Real-time Processing*
