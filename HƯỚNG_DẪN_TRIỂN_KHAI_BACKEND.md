# HƯỚNG DẪN TRIỂN KHAI VÀ CHẠY ĐẦY ĐỦ BACKEND ("VES-Monitor") PHỤC VỤ APP ANDROID

## 1️⃣ PHẦN MỀM YÊU CẦU TRÊN MÁY TÍNH

### 1. Ubuntu WSL hoặc Linux (Khuyên dùng WSL2 nếu dùng Windows)
- Windows: [Cài đặt WSL2 (Ubuntu)](https://learn.microsoft.com/en-us/windows/wsl/install)
- Linux/macOS: không cần WSL

### 2. Docker & Docker Compose
- Cài [Docker Desktop](https://www.docker.com/products/docker-desktop/) cho Win/macos.
- Ubuntu Linux:  
  ```bash
  sudo apt-get update
  sudo apt-get install docker.io docker-compose -y
  sudo usermod -aG docker $USER
  exec $SHELL  # hoặc logout/login lại
  ```

### 3. Java JDK (>= 11)
- Ubuntu:
  ```bash
  sudo apt-get install openjdk-11-jdk -y
  ```
- Kiểm tra: `java -version` và `javac -version`

### 4. Maven
- Ubuntu:
  ```bash
  sudo apt-get install maven -y
  ```
- Kiểm tra: `mvn -version`

> **Lưu ý:**  
> Nếu dùng WSL, clone code và chạy mọi lệnh trong Ubuntu terminal (không khuyến nghị Git Bash/cmd/PowerShell gốc Win)

---

## 2️⃣ CLONE MÃ NGUỒN

```bash
git clone https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres.git
cd Real-time-processing-with-Kafka-Flink-Postgres
```

---

## 3️⃣ BUILD TOÀN BỘ MÃ NGUỒN (JAR cho generator, flink job, api...)

```bash
mvn clean package -DskipTests
```
> Build thành công sẽ sinh ra các file:
> - backend-api/target/ves-backend-api.jar
> - flink-job/target/ves-flink-job-1.0.0.jar
> - data-generators/fuel-price-producer/target/fuel-price-producer-1.0.0.jar
> - data-generators/grid-load-generator/target/grid-load-generator-1.0.0.jar
> - data-generators/renewable-generator/target/renewable-generator-1.0.0.jar

---

## 4️⃣ KHỞI ĐỘNG STACK DOCKER (Postgres, Kafka, Flink, Zookeeper)

```bash
bash scripts/run.sh
```
- Chờ 1-2 phút, mọi service báo "healthy".
- Kiểm tra:
    ```bash
    docker ps
    bash scripts/healthcheck.sh
    ```

---

## 5️⃣ TẠO KAFKA TOPICS

```bash
bash scripts/create_kafka_topics.sh
```

---

## 6️⃣ CHẠY DATA GENERATORS (3 process Java sinh dữ liệu → Kafka)

```bash
bash scripts/start_generators.sh
```
- Kiểm tra file log: `/tmp/ves-logs/*.log` (phải có log mới liên tục).

---

## 7️⃣ SUBMIT FLINK JOB (bắt buộc, nếu không sẽ không có data lên database!)

- Mở Flink UI: [http://localhost:8081](http://localhost:8081)
- Bấm **"Submit New Job"**, chọn file:  
  `flink-job/target/ves-flink-job-1.0.0.jar`, submit.
- Chờ vài giây job chuyển trạng thái RUNNING.

---

## 8️⃣ CHẠY BACKEND SPRING BOOT API

```bash
java -jar backend-api/target/ves-backend-api.jar
```
- Mặc định listen port `8090`  
- Test Swagger: [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)

---

## 9️⃣ KIỂM TRA DỮ LIỆU VÀ API (tùy chọn)

- Test thử API (phải ra dữ liệu thật, không phải mảng rỗng):
    ```bash
    bash scripts/phase45_smoke_api.sh
    ```
- Dùng Postman/Curl/FE/test Android

---

## 10️⃣ TÀI KHOẢN MẶC ĐỊNH / ĐĂNG NHẬP APP

| username | password |
|----------|----------|
| admin    | admin    |
| manager  | manager  |
| user     | user     |

---

## 11️⃣ DỪNG TOÀN BỘ HỆ THỐNG

```bash
bash scripts/stop.sh
bash scripts/stop_generators.sh
```
---

## 12️⃣ LƯU Ý & XỬ LÝ LỖI PHỔ BIẾN

- Nếu API trả mảng rỗng:  
  - Kiểm tra generator, Flink job có đang chạy không
  - Kiểm tra các view trong Postgres có dữ liệu không
- Nếu Flink job “Restarting”/“Not enough resources”:
  - Kiểm tra TaskManager docker
  - Chỉ chạy 1 job/lần khi máy yếu
- Nếu không truy cập được http://localhost:8090:
  - Check log backend-api, port có bị trùng không

---

## 13️⃣ NẾU CẦN SỬA CODE GEN/PIPELINE

- Sau khi sửa, **bắt buộc build lại JAR liên quan** và restart thành phần generator hoặc api/flink job.

---

# ✅ **KẾT QUẢ**

- Android app, Postman, FE đều có thể kết nối backend qua các endpoint (Swagger đã list)
- Dữ liệu được sinh động, cập nhật liên tục, trải nghiệm chuẩn production!

---

**LIÊ**
