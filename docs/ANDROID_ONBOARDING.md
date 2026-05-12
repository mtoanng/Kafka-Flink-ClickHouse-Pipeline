# 📱 Android Developer Onboarding — Energy Security Mobile App

> Tài liệu **single-shot** (~15 phút đọc) giúp bạn nắm scope đồ án Android, hiểu cách tích hợp với REST API có sẵn, biết cần chuẩn bị những gì trước buổi kickoff.

---

## 1. Bối cảnh đồ án

Đây là **đồ án môn Android (Phát triển ứng dụng di động)** — đã có sẵn backend Spring Boot expose 14 endpoint, chỉ cần consume qua HTTP. Không cần biết Kafka/Flink/Pillar internals.

### Ứng dụng làm gì?

Mobile app cho phép người dùng (Manager / Admin năng lượng) **xem dashboard giám sát an ninh năng lượng Việt Nam** theo thời gian thực:

- Điểm an ninh năng lượng tổng hợp (Energy Security Score 0-100)
- 4 nhóm chỉ số: Nguồn cung, Giá nhiên liệu, Phụ tải lưới điện, Năng lượng tái tạo
- Danh sách cảnh báo + danh sách khuyến nghị có thể acknowledge

Không cần hiểu sâu nội tại 4 nhóm — chỉ cần biết **mỗi endpoint trả gì** (xem mục 5).

---

## 2. Vai trò & deliverables

**Android Owner** — đảm nhiệm 100% phía Android frontend:

| Mục | Nội dung |
|-----|----------|
| **Thiết kế** | Chọn stack (Kotlin/Java, MVVM/MVI, libs), thiết kế UI/UX (Material 3) |
| **Build** | 5-7 màn, auth flow, API integration, persist JWT |
| **Test** | Chạy được trên emulator + 1 điện thoại thật |
| **Tài liệu** | Báo cáo môn Android (đề tài, kiến trúc, UI/UX, demo) |
| **Demo** | Live demo + video screen record 5-7 phút |

**Leader** sẽ:
- Cung cấp **REST API endpoint** (URL + 14 endpoint sẵn sàng)
- Cung cấp **JWT token mẫu** để bạn test trước khi backend deploy public
- Review PR mỗi 2-3 ngày
- Hỗ trợ debug khi bạn gặp issue về API response/contract

**Workload dự kiến**: ~25-30 giờ trong **4 tuần**.

---

## 3. Deliverables chi tiết — 5-7 màn chính

| # | Màn / Feature | Endpoint dùng | Ghi chú |
|---|---------------|---------------|---------|
| 1 | **Splash + Login** | `POST /api/auth/login` | Logo splash 2s → form username/password → lưu JWT |
| 2 | **Home Dashboard** | `GET /api/security/score`, `/api/alerts/active`, `/api/recommendations` | 4 KPI card + Security Score gauge + bottom-nav |
| 3 | **4 tab Category** | `GET /api/pillars/{1-4}/...` | Mỗi tab 1 line/bar chart + insight text + top 5 entities |
| 4 | **Alert list** | `GET /api/alerts/active` | RecyclerView, filter theo severity |
| 5 | **Recommendation list + ACK** | `GET /api/recommendations`, `POST /api/recommendations/{id}/acknowledge` | List + button "Xác nhận" / "Bỏ qua" |
| 6 | **Settings** | (local SharedPreferences) | Dark mode toggle, đổi `BASE_URL`, đổi ngôn ngữ VN/EN, logout |
| 7 | **About** | (static) | Thông tin nhóm + version + GitHub link |

### Stretch goals (nice-to-have)

- Map screen với marker 6 region VN — `osmdroid` (free, no key) hoặc Google Maps SDK
- Local notification khi có alert mới (poll mỗi 30s)
- Đa ngôn ngữ VN + EN

### Out of scope (KHÔNG làm)

- ❌ FCM push notification
- ❌ Room offline cache (API luôn online)
- ❌ Multi-tenant / role granular permission
- ❌ Backend development (đã có)

---

## 4. Tech stack — Khuyến nghị

| Layer | Khuyến nghị | Lý do |
|-------|-------------|-------|
| **Language** | **Kotlin 1.9+** (Java cũng OK nếu bạn rành hơn) | Modern, ít boilerplate |
| **Min/Target SDK** | min 24 / target 34 | Cover 95%+ device |
| **Build** | Gradle KTS (`build.gradle.kts`) | Modern syntax |
| **UI** | **XML layouts + Material 3** | Đủ cho 7 màn. Tránh Compose nếu chưa rành (learning curve cao) |
| **Architecture** | **MVVM single-module** + ViewBinding | Simple, đủ |
| **Network** | **Retrofit 2** + OkHttp + Gson | Industry standard |
| **Async** | **Kotlin Coroutines** | Đơn giản hơn RxJava |
| **Chart** | **MPAndroidChart** | Line + Bar + Pie, free |
| **Storage** | **SharedPreferences** (JWT + settings) | Không cần Room |
| **DI** | **Manual constructor injection** (không cần Hilt) | App nhỏ, DI lib overkill |
| **Logging** | **Timber** | Hơn `Log` mặc định |
| **Test** | JUnit 4 + Mockito (5-10 unit test) | Đủ cho báo cáo |

Bạn **toàn quyền** override khuyến nghị này — báo Leader trong PR đầu để cập nhật báo cáo.

---

## 5. API Contract — 14 endpoint sẽ được consume

### 5.1 Base URL

| Môi trường | URL | Khi dùng |
|------------|-----|----------|
| **Emulator Android Studio** | `http://10.0.2.2:8090` | Dev local — `10.0.2.2` = host loopback của emulator |
| **Điện thoại thật cùng WiFi** | `http://<Leader-IP>:8090` | Demo offline trên LAN |
| **Demo public** | `https://<random>.trycloudflare.com` | Demo từ xa qua Cloudflared tunnel |

App **PHẢI** cho phép đổi `BASE_URL` qua màn Settings để switch giữa 3 môi trường mà không rebuild APK.

Lưu ý dev:
- Manifest cần `android:usesCleartextTraffic="true"` (HTTP local)
- Hoặc setup `network_security_config.xml` cho phép cleartext với specific domain

### 5.2 Authentication flow

```
[App]                                    [Backend]
  │  POST /api/auth/login                    │
  │  body: {username, password}              │
  │ ───────────────────────────────────────► │
  │                                          │
  │  200 OK                                  │
  │  body: {accessToken, expiresIn, user}    │
  │ ◄─────────────────────────────────────── │
  │                                          │
  │  Save accessToken vào SharedPreferences  │
  │                                          │
  │  GET /api/* (any other endpoint)         │
  │  header: Authorization: Bearer <token>   │
  │ ───────────────────────────────────────► │
  │                                          │
  │  200 OK / 401 Unauthorized               │
  │ ◄─────────────────────────────────────── │
  │                                          │
  │  Nếu 401 → clear token → quay về Login   │
```

- **Token TTL**: 8 giờ (28800s) — đủ cho 1 buổi demo + dev session
- **Seed users** Leader cung cấp:
  - `admin` / `admin` — full access
  - `manager` / `manager` — xem + acknowledge
  - `viewer` / `viewer` — read-only

### 5.3 Bảng 14 endpoint

| # | Method | Path | Mô tả | Auth |
|---|--------|------|-------|------|
| 1 | POST | `/api/auth/login` | Đăng nhập, trả JWT | Public |
| 2 | GET | `/api/security/score` | Energy Security Score 0-100 + 4 sub-score | ✅ |
| 3 | GET | `/api/security/cascade-risks` | Rủi ro tổng hợp đa pillar | ✅ |
| 4 | GET | `/api/pillars/1/outlook` | Pillar 1 — Dự báo tồn kho nhiên liệu | ✅ |
| 5 | GET | `/api/pillars/2/volatility` | Pillar 2 — Biến động giá nhiên liệu | ✅ |
| 6 | GET | `/api/pillars/3/shedding-plan` | Pillar 3 — Plan ngắt tải lưới điện | ✅ |
| 7 | GET | `/api/pillars/4/net-zero-progress` | Pillar 4 — Tiến độ phát thải / Net Zero | ✅ |
| 8 | GET | `/api/alerts/active` | Danh sách cảnh báo đang hoạt động | ✅ |
| 9 | GET | `/api/recommendations?status=PENDING` | Danh sách khuyến nghị | ✅ |
| 10 | POST | `/api/recommendations/{id}/acknowledge` | Xác nhận khuyến nghị | ✅ (manager+) |
| 11 | GET | `/api/raw/fuel-prices/latest?limit=50` | Giá nhiên liệu gần nhất | ✅ |
| 12 | GET | `/api/raw/grid-load/latest?region=VN_NORTH` | Tải lưới gần nhất | ✅ |
| 13 | GET | `/api/health` | Health check (DB ping) | Public |
| 14 | GET | `/v3/api-docs` | OpenAPI spec — import vào Postman / OpenAPI Generator | Public |

### 5.4 Sample response — bộ DTO chính

**`POST /api/auth/login` response**:
```json
{
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 28800,
  "user": {
    "id": 1, "username": "admin",
    "fullName": "Administrator", "email": "admin@ves.local",
    "role": "ADMIN"
  }
}
```

**`GET /api/security/score`**:
```json
{
  "overallScore": 76.4,
  "status": "STABLE",                 // SECURE | STABLE | AT_RISK | CRITICAL
  "pillar1Score": 82.0,
  "pillar2Score": 68.5,
  "pillar3Score": 75.0,
  "pillar4Score": 80.0,
  "calculatedAt": "2026-05-12T10:30:00Z",
  "trend": "DECREASING"               // INCREASING | STABLE | DECREASING
}
```

**`GET /api/alerts/active`**:
```json
[
  {
    "id": 123,
    "metricType": "GRID_LOAD_PCT",    // FUEL_PRICE | GRID_LOAD_PCT | EMISSION_INTENSITY | INVENTORY_DAYS
    "fuelType": null,
    "region": "VN_HANOI",
    "severity": "CRITICAL",           // INFO | WARNING | CRITICAL
    "message": "Grid load Hà Nội = 92.5% > threshold 92%",
    "triggeredValue": 92.5,
    "threshold": 92.0,
    "eventTimestamp": "2026-05-12T10:25:00Z",
    "alertTimestamp": "2026-05-12T10:25:03Z"
  }
]
```

**`GET /api/recommendations`**:
```json
[
  {
    "id": 42,
    "pillar": 1,                      // 1 | 2 | 3 | 4
    "actionType": "TRANSFER_STOCK",
    "severity": "WARNING",
    "title": "Chuyển 5000 KL Gasoline NINHTHUAN → HANOI",
    "message": "Hà Nội còn 56.6 ngày tồn kho, dưới target 90...",
    "suggestedData": {                // JSON object, structure tuỳ actionType
      "from": "VN_NINHTHUAN",
      "to": "VN_HANOI",
      "volumeKl": 5000
    },
    "status": "PENDING",              // PENDING | ACKNOWLEDGED | DISMISSED | EXPIRED
    "suggestedAt": "2026-05-12T08:15:00Z",
    "expiresAt": "2026-05-19T08:15:00Z"
  }
]
```

**`POST /api/recommendations/{id}/acknowledge` request**:
```json
{
  "note": "Đã chuyển stock theo plan, hoàn tất 2026-05-13"
}
```

**`GET /api/pillars/1/outlook` (sample)**:
```json
[
  {
    "regionCode": "VN_HANOI",
    "fuelType": "GASOLINE",
    "stockDays": 56.6,
    "targetDays": 90,
    "deficitDays": 33.4,
    "supplyStatus": "WARNING",       // SECURE | WARNING | CRITICAL
    "recommendationText": "Chuyển 5000 KL từ Ninh Thuận..."
  }
]
```

> 📦 **Tip**: khi backend Phase 4.5 build xong, bạn có thể import OpenAPI spec từ `http://10.0.2.2:8090/v3/api-docs` → **auto-generate Retrofit interfaces** bằng OpenAPI Generator CLI để tiết kiệm thời gian gõ DTO.

### 5.5 Error format

```json
{
  "timestamp": "2026-05-12T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token expired",
  "path": "/api/security/score"
}
```

Xử lý chung:
- **401** → token hết hạn → clear SharedPreferences → quay về Login
- **403** → role không đủ quyền → snackbar "Không đủ quyền"
- **5xx** → snackbar "Lỗi server, vui lòng thử lại" + Timber log

### 5.6 Khi backend chưa deploy

Trong 1-2 tuần đầu khi chưa build Phase 4.5 (proxy block), bạn có 2 option:

**Option A — Mock server bằng `json-server`** (5 phút setup):
```bash
npx json-server --watch mock-api.json --port 8090
```
Sẽ được cung cấp file `mock-api.json` chứa response mẫu cho 14 endpoint.

**Option B — Postman Mock Server** (free 1000 calls/tháng):
- Leader share Postman workspace
- Bạn dùng mock URL `https://<random>.mock.pstmn.io`

Phù hợp cho dev UI/UX trước, integration thật khi backend ready.

---

## 6. Chuẩn bị trước Tuần 1 — Checklist

### 6.1 Phần mềm

| Tool | Version | Link | Lý do |
|------|---------|------|-------|
| **Android Studio** | Hedgehog 2023.1+ | https://developer.android.com/studio | IDE chính |
| **JDK** | 17 (bundled) | (đi kèm AS) | Build Gradle |
| **Android SDK** | API 24-34 | (cài qua SDK Manager) | Compile + emulate |
| **Emulator** | Pixel 5 API 34 | (cài qua AVD Manager) | Test |
| **Git** | 2.40+ | https://git-scm.com/downloads | Version control |
| **Postman** | latest | https://www.postman.com/downloads/ | Test API |
| **Node.js** | 18+ (optional) | https://nodejs.org/ | Cho `json-server` mock + OpenAPI Generator |

### 6.2 Setup Android Studio (1 lần)

```text
Settings → Plugins → tìm và cài (optional):
  • Material Design Icon Generator
  • JSON To Kotlin Class
  • Rainbow Brackets

Settings → Editor → Code Style → Kotlin → Indent: 4

Settings → Tools → Actions on Save:
  ✅ Reformat code
  ✅ Optimize imports

Settings → Version Control → Git → ✅ Enable staging area
```

### 6.3 Tài khoản cần có

| Account | Mục đích | Bắt buộc? |
|---------|----------|-----------|
| **GitHub** | Push code repo Android riêng của bạn | ✅ |
| **Google account** | Sign in Android emulator | ✅ |
| **Postman free** | Save API collection mẫu | ✅ |

### 6.4 Access cần xin Leader

- [ ] **JWT token mẫu** (8 giờ) để test Retrofit interface trước khi backend deploy
- [ ] **OpenAPI spec file** `api-docs.json` (Leader export từ Spring Boot)
- [ ] **File `mock-api.json`** cho json-server (Leader chuẩn bị)
- [ ] **BASE_URL backend** sau Phase 4.5 build (tạm thời mock, sau update)
- [ ] **Group chat** (Zalo/Discord) — daily standup 5'
- [ ] **GitHub repo Java reference** (read-only) — nếu bạn muốn xem cách Leader scaffold backend làm reference

### 6.5 Kiến thức cần review (3-4 giờ)

5 link must-read trước khi code:

1. **Retrofit basics** (45') — https://square.github.io/retrofit/
2. **MVVM Android** (45') — https://developer.android.com/topic/architecture
3. **Kotlin Coroutines for Android** (1h) — https://developer.android.com/kotlin/coroutines
4. **Material 3 components** (30') — https://m3.material.io/components
5. **MPAndroidChart Getting Started** (30') — https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started
6. **Bottom Navigation + Fragment** (30') — https://developer.android.com/develop/ui/views/navigation/bottom-navigation

> 💡 Vừa đọc vừa tạo project sandbox `HelloVesMonitor/` để thử Retrofit + ViewModel + Coroutines chạy trên emulator. Khi start dự án chính, copy pattern sang.

---

## 7. First-day setup (Tuần 1, ngày 1)

```bash
# 1. Đọc file này + 1 cuộc kickoff với Leader (mục 11)

# 2. Tạo repo Android RIÊNG trên GitHub
#    Tên gợi ý: ves-monitor-mobile
#    Visibility: Private (sau dự án có thể đổi Public)
mkdir ves-monitor-mobile
cd ves-monitor-mobile
git init
git remote add origin https://github.com/<your-username>/ves-monitor-mobile.git

# 3. Mở Android Studio → New Project → Empty Activity
#    Name: VES Monitor Mobile
#    Package: vn.edu.ves.mobile (hoặc package của bạn)
#    Language: Kotlin
#    Min SDK: API 24
#    Build configuration: Kotlin DSL
#    Save vào: ves-monitor-mobile/

# 4. Sync Gradle → đợi 5-10' lần đầu

# 5. Test "Hello Android" trên emulator → OK

# 6. Test API connection (giai đoạn này backend chưa build, dùng mock)
#    Option A: chạy json-server local với file mock-api.json Leader gửi
#    Option B: dùng Postman Mock Server URL Leader share

# 7. Tạo branch + push lần đầu
git checkout -b main
git add .
git commit -m "Initial Android Studio bootstrap"
git push -u origin main

# 8. Tạo branch feature đầu tiên
git checkout -b feat/login-flow
```

---

## 8. Recommended file structure (Kotlin + MVVM)

```
ves-monitor-mobile/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       │
│       ├── java/vn/edu/ves/mobile/
│       │   ├── VesMonitorApp.kt         # Application class
│       │   │
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   ├── ApiClient.kt     # Retrofit builder + dynamic BASE_URL
│       │   │   │   ├── ApiService.kt    # @GET/@POST interface (14 endpoint)
│       │   │   │   └── AuthInterceptor.kt
│       │   │   ├── dto/                 # 14+ DTO matching backend
│       │   │   │   ├── LoginRequest.kt, LoginResponse.kt
│       │   │   │   ├── SecurityScoreDto.kt, CascadeRiskDto.kt
│       │   │   │   ├── Pillar1OutlookDto.kt ... Pillar4
│       │   │   │   ├── AlertDto.kt, RecommendationDto.kt
│       │   │   │   └── ErrorResponse.kt
│       │   │   ├── prefs/
│       │   │   │   └── SessionManager.kt  # Wrap SharedPreferences (JWT + user + BASE_URL)
│       │   │   └── repo/
│       │   │       ├── AuthRepository.kt
│       │   │       ├── PillarRepository.kt
│       │   │       ├── RecommendationRepository.kt
│       │   │       └── AlertRepository.kt
│       │   │
│       │   ├── ui/
│       │   │   ├── splash/SplashActivity.kt
│       │   │   ├── login/{LoginActivity, LoginViewModel}.kt
│       │   │   ├── main/{MainActivity, MainViewModel}.kt  # Host bottom-nav
│       │   │   ├── home/{HomeFragment, HomeViewModel}.kt
│       │   │   ├── pillar1/{Pillar1Fragment, Pillar1ViewModel}.kt
│       │   │   ├── pillar2/ ... pillar4/
│       │   │   ├── alerts/{AlertListFragment, AlertAdapter, AlertViewModel}.kt
│       │   │   ├── recommendations/{RecListFragment, RecAdapter, RecViewModel}.kt
│       │   │   ├── settings/SettingsActivity.kt
│       │   │   └── about/AboutActivity.kt
│       │   │
│       │   └── util/
│       │       ├── Result.kt             # sealed class Loading/Success/Error
│       │       ├── DateFormat.kt
│       │       └── Extensions.kt
│       │
│       └── res/
│           ├── layout/                   # XML layouts (8-10 file)
│           ├── values/
│           │   ├── strings.xml, colors.xml, themes.xml, dimens.xml
│           ├── values-en/strings.xml     # English
│           ├── drawable/                 # icons, splash background
│           ├── mipmap-*/                 # launcher icons
│           └── menu/                     # bottom-nav, toolbar
│
├── gradle/
├── build.gradle.kts, settings.gradle.kts
├── README.md
├── .gitignore                            # /build/, /.idea/, local.properties
└── docs/
    ├── REPORT.md                         # Báo cáo môn Android
    ├── screenshots/                      # ≥ 10 PNG dùng cho slide
    └── demo-script.md                    # Kịch bản video demo
```

---

## 9. Gradle dependencies tham khảo (`app/build.gradle.kts`)

```kotlin
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "vn.edu.ves.mobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { viewBinding = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation (cho bottom-nav fragment)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Retrofit + OkHttp + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

```kotlin
// settings.gradle.kts hoặc root build.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")  // MPAndroidChart cần JitPack
    }
}
```

> ⚠️ **Lưu ý nếu dev trên máy Bosch có NTLM proxy** (cùng môi trường Leader): set `~/.gradle/gradle.properties`:
> ```
> systemProp.https.proxyHost=rb-proxy-apac.bosch.com
> systemProp.https.proxyPort=8080
> ```
> Hoặc dùng hotspot 4G khi sync Gradle lần đầu (cache xong là OK).

---

## 10. PR & workflow

### 10.1 Branch convention (repo Android riêng của bạn)

- `main` — protected, chỉ merge qua PR
- `feat/<feature-name>` — VD `feat/login-flow`, `feat/home-dashboard`
- `fix/<bug>` — bug fix
- `chore/<task>` — refactor, doc update

### 10.2 PR target (5-7 PR cho ~30h)

| # | PR | Mô tả | Time |
|---|----|----|-------|
| 1 | Bootstrap | Gradle + Retrofit + ApiClient + SessionManager + Application | 3h |
| 2 | Splash + Login | SplashActivity + LoginActivity + ViewModel + AuthRepository + persist JWT | 4h |
| 3 | Main shell + Home | MainActivity (bottom-nav) + HomeFragment + 4 KPI card + Security Score gauge | 5h |
| 4 | 4 Pillar tabs | 4 Fragment + ViewModel + Repository + MPAndroidChart mỗi tab | 7h |
| 5 | Alerts + Recommendations | 2 Fragment + Adapter + filter + ACK + snackbar | 5h |
| 6 | Settings + About + i18n | Settings (dark + URL + lang) + About + VN/EN | 3h |
| 7 | Polish + build APK + demo prep | Fix bug + screenshot + APK release-debug + demo trên điện thoại | 3h |

### 10.3 Daily standup (5 phút)

Báo trong group chat:
- Hôm qua xong gì?
- Hôm nay sẽ làm gì?
- Có blocker không?

### 10.4 Khi cần help

| Vấn đề | Hỏi ai | Cách |
|--------|--------|------|
| API response không match DTO | Leader | Tag PR + paste curl response + DTO code |
| Auth/JWT logic không hiểu | Leader | Group chat |
| Backend đang down/lỗi | Leader | Group chat — Leader restart |
| Android-specific bug (gradle, emulator) | Self / Stack Overflow / Cursor AI | Quy tắc 2 giờ — sau 2h vẫn bí mới ping |

---

## 11. Kickoff agenda — Meeting đầu với Leader (45 phút)

| # | Topic | Time | Ai chuẩn bị |
|---|-------|------|-------------|
| 1 | Giới thiệu nhau + workflow team | 5' | Cả hai |
| 2 | Demo backend hiện tại qua Swagger UI (Leader screen share) | 10' | Leader |
| 3 | Walk through 14 endpoint contract qua Postman | 10' | Leader |
| 4 | Bạn trình bày stack đã chọn + mockup nhanh | 10' | **BẠN** |
| 5 | Thống nhất 5-7 PR + deadline + cách review | 5' | Cùng |
| 6 | Setup giai đoạn mock (json-server hoặc Postman Mock) | 5' | Leader cung cấp file |

### Câu hỏi đề xuất hỏi Leader

- [ ] Khi nào backend Phase 4.5 build xong (để có endpoint thật)?
- [ ] Trong khi chờ, dùng json-server hay Postman Mock?
- [ ] Em dùng Kotlin được không? (Java có dễ review hơn cho anh không?)
- [ ] App có yêu cầu dark mode bắt buộc không?
- [ ] Có cần màn Map không? Nếu có, OSM (free) hay Google Maps SDK (cần API key)?
- [ ] Deadline đồ án Android cứng là tuần mấy?
- [ ] Có buổi dress rehearsal trước bảo vệ không?
- [ ] Khi demo, app sẽ chạy:
  - (a) Trên emulator của máy demo → cần backend Leader chạy local + tunnel
  - (b) Trên điện thoại em → cần điện thoại + tunnel
  - (c) Cả hai → có backup nếu 1 cái fail

---

## 12. Demo & deliverables cuối kỳ

### 12.1 Phần demo bạn phụ trách (~2-3 phút trong buổi bảo vệ)

1. Mở app → Login `admin/admin`
2. Home → chỉ Security Score + 3 KPI
3. Switch sang tab Pillar 3 → chỉ chart phụ tải + insight
4. Tab Alerts → list 5 alert đa nhóm
5. Tab Recommendations → tap ACK 1 cái → snackbar success
6. (Bonus) Settings → toggle dark mode

### 12.2 APK release-debug

Build `app-debug.apk` + upload vào repo Android (folder `releases/` hoặc GitHub Release) → giảng viên cài thử trên điện thoại của họ.

### 12.3 Báo cáo môn Android

Theo cấu trúc môn Mobile của trường (hỏi giáo viên). Gợi ý nội dung tối thiểu:
- Chương 1: Tổng quan đề tài + bài toán
- Chương 2: Phân tích yêu cầu + use-case + mockup UI
- Chương 3: Kiến trúc (MVVM diagram) + công nghệ (Retrofit/Coroutines/Material 3)
- Chương 4: Demo screenshot + chức năng từng màn
- Chương 5: Quản lý dự án (Leader + bạn) + tiến độ + GitHub Insights
- Chương 6: Kết quả + hạn chế + hướng phát triển
- Phụ lục: AI Usage Log

---

## 13. Tóm tắt — Bạn cần làm gì ngay?

1. **Đọc file này** (~15')
2. **Cài Android Studio** + tạo emulator Pixel 5 API 34 (~30')
3. **Review 5 link kiến thức** ở mục 6.5 (~3-4h rải rác)
4. **Lên agenda kickoff** với Leader (mục 11) — đề xuất 1 buổi tuần này
5. **Tạo repo Android riêng** trên GitHub (`ves-monitor-mobile` hoặc tên bạn thích)
6. **Đề nghị Leader gửi**: JWT token mẫu, OpenAPI spec, file mock JSON
7. **Setup project bootstrap** + push commit đầu

> 🎯 Welcome aboard! Mọi câu hỏi sau khi đọc, ping Leader hoặc group chat. Chúc bạn build app vui & qua môn điểm cao 🎓
