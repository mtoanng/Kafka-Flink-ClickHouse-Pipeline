# 🌐 backend-api — Spring Boot REST API (Phase 4.5)

> 1 module Spring Boot 2.7 / Java 11, **14 endpoint** cover 4 pillar an ninh năng lượng + JWT auth + Swagger UI. Đọc dữ liệu trực tiếp từ Postgres views đã build sẵn ở Phase 2.5 + 2.6 + 3 + 4 — không có business logic mới, chỉ là **REST gateway** cho JavaFX Desktop (Phase 5) và Android (Phase 6).

---

## 📋 Endpoint summary

| # | Method | Path | Auth | Mô tả |
|---|--------|------|------|-------|
| 1 | `GET`  | `/api/health` | ❌ | DB ping, dùng cho healthcheck script & tunnel probe |
| 2 | `POST` | `/api/auth/login` | ❌ | Đăng nhập (`{username, password}`) → JWT |
| 3 | `GET`  | `/api/auth/me` | ✅ | User hiện tại theo JWT |
| 4 | `GET`  | `/api/pillars/1/outlook` | ✅ | Pillar 1 supply outlook (`v_pillar1_supply_outlook`) |
| 5 | `GET`  | `/api/pillars/2/volatility` | ✅ | Pillar 2 σ rolling signal |
| 6 | `GET`  | `/api/pillars/3/shedding-plan` | ✅ | Pillar 3 load shedding priority list |
| 7 | `GET`  | `/api/pillars/4/net-zero` | ✅ | Pillar 4 renewable vs roadmap 2026/2030 |
| 8 | `GET`  | `/api/security/score` | ✅ | Light ESI 0-100 + status SECURE/STABLE/AT_RISK/CRITICAL |
| 9 | `GET`  | `/api/security/cascade-risks` | ✅ | Cảnh báo compound multi-pillar |
| 10| `GET`  | `/api/recommendations?limit=N` | ✅ | List PENDING recommendations |
| 11| `POST` | `/api/recommendations/{id}/acknowledge` | ✅ | ACK / DISMISS + audit (user, note) |
| 12| `GET`  | `/api/alerts/active?limit=N` | ✅ | Cảnh báo chưa acknowledge, đa pillar |
| 13| `GET`  | `/api/fuel-prices/latest?fuel_type=&limit=` | ✅ | N giá nhiên liệu mới nhất (Pillar 2) |
| 14| `GET`  | `/api/grid-load/latest` | ✅ | Phụ tải mới nhất per region (Pillar 3) |

📖 **Swagger UI**: `http://localhost:8090/swagger-ui.html` (có nút Authorize để dán Bearer token).

---

## 🔑 Auth & seed users (Phase 2 seed)

| username | password | role    |
|----------|----------|---------|
| `admin`   | `admin`   | `ADMIN`   |
| `manager` | `manager` | `MANAGER` |
| `user`    | `user`    | `VIEWER`  |

> Password đã bcrypt sẵn ở `infra/script/04_seed_basic.sql`. Spring `BCryptPasswordEncoder` match cả `$2a$` và `$2b$` hash.
> JWT token sống 8h cho dev demo (cấu hình `ves.security.jwt.expiration-ms` trong `application.yml`).

---

## 🚀 Quick run (sau khi đã build JAR)

```bash
# Prerequisite: Docker stack đang chạy (Postgres healthy)
bash scripts/run.sh                              # nếu chưa chạy

# 1. Build JAR (cần network — xem mục Build dưới)
mvn -pl backend-api -am clean package -DskipTests

# 2. Run app (port 8090)
java -jar backend-api/target/ves-backend-api.jar

# 3. Smoke test 14 endpoint (terminal khác)
bash scripts/phase45_smoke_api.sh
```

Run ngầm để giữ shell rảnh:
```bash
nohup java -jar backend-api/target/ves-backend-api.jar > /tmp/ves-api.log 2>&1 &
echo $! > /tmp/ves-api.pid
# stop:  kill $(cat /tmp/ves-api.pid)
```

---

## 🛠️ Build từ source

```bash
mvn -pl backend-api -am clean package -DskipTests
```

Output: `backend-api/target/ves-backend-api.jar` (~30-35 MB, fat JAR đã repackage Spring Boot).

### Dependencies tải lần đầu (~80 MB qua proxy)

- Spring Boot 2.7.18 (web, security, jdbc, validation, test)
- io.jsonwebtoken (jjwt) 0.11.5
- springdoc-openapi-ui 1.7.0
- Lombok 1.18.30
- PostgreSQL JDBC 42.7.3 (đã cache từ Phase 0)

### ⚠️ Mạng công ty Bosch — proxy NTLM

Proxy `rb-proxy-apac.bosch.com:8080` của Bosch dùng **NTLM/Negotiate**, Maven Wagon mặc định KHÔNG support. Một số cách workaround:

1. **Hotspot điện thoại** (sạch nhất): tắt VPN/proxy, kết nối 4G, `unset HTTPS_PROXY HTTP_PROXY https_proxy http_proxy` rồi `mvn package`. Sau khi cache đầy đủ vào `~/.m2/repository`, có thể quay lại Bosch network và build offline.

2. **cntlm local bridge** (`sudo apt install cntlm`, config NTLM v2 hash, point Maven → `http://localhost:3128`).

3. **px-proxy** (Python NTLM proxy, hỗ trợ SSPI trên Windows).

4. **JFrog Artifactory / Nexus nội bộ Bosch** nếu công ty có repo mirror — sửa `<mirrors>` trong `~/.m2/settings.xml`.

Khi build thành công offline, mọi command đều chạy được không cần network (Spring Boot khởi động chỉ cần DB qua JDBC localhost).

---

## ⚙️ Cấu hình (`src/main/resources/application.yml`)

| Property | Default | Override env |
|----------|---------|--------------|
| `spring.datasource.url`     | `jdbc:postgresql://localhost:5432/fuel_prices` | `SPRING_DATASOURCE_URL` |
| `spring.datasource.username` | `postgres` | `SPRING_DATASOURCE_USERNAME` |
| `spring.datasource.password` | `123456`  | `SPRING_DATASOURCE_PASSWORD` |
| `server.port` | `8090` | `SERVER_PORT` |
| `ves.security.jwt.secret` | base64 256-bit demo | `VES_SECURITY_JWT_SECRET` |
| `ves.security.jwt.expiration-ms` | 28_800_000 (8h) | `VES_SECURITY_JWT_EXPIRATION_MS` |

> **Production**: bắt buộc override `ves.security.jwt.secret` (≥ 32 ký tự ASCII), hạ TTL xuống 30-60 phút, đổi DB password.

---

## 📐 Bố cục module

```
backend-api/
├── pom.xml
├── README.md (file này)
└── src/main/
    ├── java/vn/edu/ves/api/
    │   ├── VesApiApplication.java         # Spring Boot main
    │   ├── config/
    │   │   ├── SecurityConfig.java        # stateless JWT, /api/auth/login + /api/health public
    │   │   ├── JwtTokenProvider.java      # HS256 sign/parse
    │   │   ├── JwtAuthFilter.java         # OncePerRequestFilter — đọc Bearer
    │   │   └── OpenApiConfig.java         # Swagger metadata + Bearer scheme
    │   ├── controller/                    # 6 controller / 14 endpoint
    │   │   ├── AuthController.java
    │   │   ├── PillarController.java      # /api/pillars/{1..4}/...
    │   │   ├── SecurityController.java    # /api/security/{score, cascade-risks}
    │   │   ├── RecommendationController.java
    │   │   ├── AlertController.java
    │   │   ├── RawDataController.java     # /api/fuel-prices/latest + /api/grid-load/latest
    │   │   └── HealthController.java
    │   ├── dao/                           # 5 DAO JdbcTemplate
    │   │   ├── UserDao.java
    │   │   ├── PillarDao.java             # 4 view pillar + fuel_prices_raw + v_pillar3_grid_load_latest
    │   │   ├── SecurityDao.java
    │   │   ├── RecommendationDao.java
    │   │   └── AlertDao.java
    │   ├── dto/                           # 11 DTO record-like
    │   │   ├── LoginRequest, LoginResponse, UserDto, AckRequest
    │   │   ├── Pillar1OutlookDto, Pillar2VolatilityDto, Pillar3SheddingDto, Pillar4NetZeroDto
    │   │   ├── SecurityScoreDto, CascadeRiskDto, RecommendationDto, AlertDto
    │   │   ├── FuelPriceDto, GridLoadLatestDto
    │   └── exception/
    │       ├── ApiException.java           # 4xx business errors
    │       └── GlobalExceptionHandler.java # @RestControllerAdvice → JSON 4xx/5xx
    └── resources/application.yml
```

---

## 🧪 Test workflow

```bash
# 1. Login
TOKEN=$(curl -sS -H 'Content-Type: application/json' \
        -d '{"username":"admin","password":"admin"}' \
        http://localhost:8090/api/auth/login | grep -oP '"accessToken":"\K[^"]+')

# 2. Test mọi endpoint cần auth
for path in \
  /api/auth/me \
  /api/pillars/1/outlook /api/pillars/2/volatility /api/pillars/3/shedding-plan /api/pillars/4/net-zero \
  /api/security/score /api/security/cascade-risks \
  /api/recommendations /api/alerts/active /api/fuel-prices/latest /api/grid-load/latest; do
    echo "=== $path ==="
    curl -sS -H "Authorization: Bearer $TOKEN" "http://localhost:8090$path" | head -c 200
    echo ""
done

# 3. Acknowledge một recommendation
curl -X POST -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
     -d '{"note":"OK"}' http://localhost:8090/api/recommendations/1/acknowledge
```

Hoặc dùng helper `scripts/phase45_smoke_api.sh` (14 endpoint × 1 click).

---

## 🐛 Common pitfalls

| Lỗi | Nguyên nhân & fix |
|-----|-------------------|
| `401 UNAUTHORIZED` ở mọi endpoint | Quên header `Authorization: Bearer ...` hoặc token expired (8h). Login lại. |
| Login trả `401 Sai username hoặc password` dù gõ đúng | Bcrypt hash trong DB có thể đã được seed bằng tool khác. Verify: `SELECT username, LEFT(password_hash,4) FROM users` → phải thấy `$2b$` hoặc `$2a$`. |
| `500 INTERNAL_ERROR` khi `/api/pillars/3/shedding-plan` | View chỉ trả data khi có generator đang chạy + load_pct ≥ 85%. Trong điều kiện bình thường endpoint sẽ trả `[]`, KHÔNG phải 500. Nếu 500 → check `docker logs postgres-database`. |
| CORS lỗi từ Android emulator | Đã `setAllowedOriginPatterns("*")` trong `SecurityConfig`. Verify request đi đến `http://10.0.2.2:8090` (không `localhost`). |
| Cloudflare tunnel: 502 sau 30s | Token đi qua tunnel có header `Authorization` bị strip nếu config sai. Dùng `cloudflared tunnel --url http://localhost:8090 --no-tls-verify` cho dev. |
| Port 8090 bị chiếm | Đổi `server.port` trong `application.yml` hoặc `SERVER_PORT=8091 java -jar ...`. |

---

## 🔜 Phase tiếp theo dùng API này

- **Phase 5 (JavaFX)**: dùng `HttpURLConnection`/OkHttp gọi 14 endpoint → render TabPane 4 pillar + recommendations sidebar.
- **Phase 6 (Android)**: Retrofit 2 + Authenticator giữ JWT trong SharedPreferences → bottom-nav 4 màn pillar.
- **Phase 7 (Deploy)**: cloudflared tunnel expose `http://localhost:8090` qua `*.trycloudflare.com` cho điện thoại thật.
