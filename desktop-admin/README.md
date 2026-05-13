# 🖥️ VES Admin Desktop (JavaFX)

> **Phase 5** của UPGRADE_PLAN §24.5 — JavaFX 17 admin desktop, 3-layer, JDBC trực tiếp Postgres.

[![Status](https://img.shields.io/badge/status-v1.0.0%20release-brightgreen)]() [![Java](https://img.shields.io/badge/Java-11-orange)]() [![JavaFX](https://img.shields.io/badge/JavaFX-17.0.10%20LTS-purple)]() [![Tests](https://img.shields.io/badge/tests-77%2F77%20PASS-success)]()

---

## 🎯 Mục tiêu

5 màn theo §24.5 — **đã hoàn thành code Phase 5.1 → 5.5**:

1. **Login** ✅ — BCrypt verify seed users (admin/manager/viewer từ Phase 2), `LoginController` switch scene → Dashboard
2. **Dashboard** ✅ — Security Score `ProgressIndicator` gauge ở top bar + TabPane 4 pillar (mỗi tab TableView + chart) + Recommendations sidebar, auto-refresh 30s
3. **Region CRUD** ✅ — form (code/name/country/vn_zone/description) + TableView, validator chain
4. **AlertRule CRUD** ✅ — multi-pillar metric_type, operator/severity enum, toggle enable/disable, threshold validator
5. **User CRUD** ✅ — ADMIN-only screen, đổi password optional, self-delete guard

3-layer pattern: **Controller (FXML) → Service → DAO → JDBC**.

### Design patterns đã triển khai

| Pattern | Vị trí | Mô tả |
|---------|--------|-------|
| **Singleton** | `DatabaseConfig`, `SessionManager` | Eager init, thread-safe AtomicReference cho user |
| **DAO** | `UserDao`, `RegionDao`, `AlertRuleDao`, `ViewsDao` | Try-with-resources, BaseDao helpers nullable-aware |
| **MVC** | `controller/*.java` + `fxml/*.fxml` + `model/*.java` | FXMLLoader + Scene switching |
| **Strategy** | `util/Validator` | Interface + `NotBlankValidator` / `PatternValidator` / `LengthRangeValidator` / `InSetValidator` |
| **Composite** | `Validator.compose(...)` | Gom nhiều Strategy thành 1 chain, collect all errors |
| **Factory** | `DatabaseConfig.openConnection()` | Trả Connection mới mỗi call (caller try-with-resources đóng) |

---

## 🚀 Quick run

### Prereq
- JDK 11+
- Maven 3.8+
- Postgres đang chạy (qua `bash scripts/run.sh` trong WSL, hoặc local Postgres)

### Build + run

```bash
# Từ root repo
mvn -pl desktop-admin -am clean compile
mvn -pl desktop-admin javafx:run
```

> ✅ **Tests verified**: `mvn -pl desktop-admin -am clean test` → **77 / 77 PASS** in ~18 s (24 DAO integration + 22 service unit + 16 util unit + 15 widget / Flink client). NTLM-proxy notice (Phase 5.0 era) đã hết hiệu lực — JavaFX 17 deps đã cache trong `~/.m2/` sau lần build đầu.

### Override DB connection

```bash
# Env vars
DB_URL=jdbc:postgresql://192.168.1.100:5432/fuel_prices DB_USER=demo DB_PASSWORD=xxx \
    mvn -pl desktop-admin javafx:run

# System properties
mvn -pl desktop-admin javafx:run \
    -Ddb.url=jdbc:postgresql://localhost:5432/test_db
```

### Seed users để login

| Username | Password | Role | Có thể truy cập |
|----------|----------|------|------------------|
| `admin`   | `admin`   | ADMIN   | Full CRUD: Region + AlertRule + User |
| `manager` | `manager` | MANAGER | CRUD Region + AlertRule, không User |
| `user`    | `user`    | VIEWER  | Chỉ xem dashboard (form CRUD disabled) |

(Hash `$2b$10$...` đã seed sẵn ở `infra/script/04_seed_basic.sql`.)

---

## 📂 Cấu trúc module

```
desktop-admin/
├── pom.xml
├── README.md (file này)
├── .gitignore
└── src/
    ├── main/
    │   ├── java/vn/edu/ves/desktop/
    │   │   ├── MainApp.java                Application entry, load login.fxml
    │   │   ├── controller/
    │   │   │   ├── LoginController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── MapsController.java
    │   │   │   ├── RegionController.java
    │   │   │   ├── AlertRuleController.java
    │   │   │   └── UserController.java
    │   │   ├── service/
    │   │   │   ├── AuthService + AuthServiceImpl
    │   │   │   ├── DashboardService + DashboardServiceImpl
    │   │   │   ├── RegionService + RegionServiceImpl
    │   │   │   ├── AlertRuleService + AlertRuleServiceImpl
    │   │   │   └── UserService + UserServiceImpl
    │   │   ├── dao/
    │   │   │   ├── BaseDao.java            Helper static methods
    │   │   │   ├── UserDao.java
    │   │   │   ├── RegionDao.java
    │   │   │   ├── AlertRuleDao.java
    │   │   │   └── ViewsDao.java           Read-only DAO cho 6 view actionable
    │   │   ├── model/
    │   │   │   ├── User, Role
    │   │   │   ├── Region
    │   │   │   ├── AlertRule, Operator, Severity
    │   │   │   ├── SecurityScore
    │   │   │   ├── Pillar1SupplySecurity, Pillar2MarketResilience,
    │   │   │   │   Pillar3GridReliability, Pillar4EnergyTransition  (Phase 7.1 IEA/APERC rename)
    │   │   │   └── Recommendation
    │   │   ├── util/
    │   │   │   ├── DatabaseConfig.java     Singleton config + Connection factory
    │   │   │   ├── SessionManager.java     Singleton, AtomicReference user slot
    │   │   │   ├── PasswordUtil.java       BCrypt wrapper
    │   │   │   ├── AlertHelper.java        JavaFX Alert wrappers
    │   │   │   └── Validator.java          Strategy + Composite
    │   │   └── exception/
    │   │       └── AuthenticationException.java
    │   └── resources/
    │       ├── application.properties      DB defaults
    │       ├── logback.xml                 Logging
    │       ├── fxml/
    │       │   ├── login.fxml
    │       │   ├── dashboard.fxml
    │       │   ├── maps.fxml
    │       │   ├── region.fxml
    │       │   ├── alertRule.fxml
    │       │   └── user.fxml
    │       └── css/material.css            Material-inspired theme
    └── test/
        └── java/vn/edu/ves/desktop/
            ├── dao/
            │   ├── H2TestSupport.java       H2 in-memory + reflection override config
            │   ├── UserDaoTest.java         7 tests
            │   ├── RegionDaoTest.java       6 tests
            │   ├── AlertRuleDaoTest.java    5 tests
            │   └── ViewsDaoTest.java        6 tests
            ├── service/
            │   ├── AuthServiceTest.java     6 tests
            │   ├── DashboardServiceTest     4 tests
            │   ├── RegionServiceTest.java   4 tests
            │   ├── AlertRuleServiceTest     3 tests
            │   └── UserServiceTest.java     5 tests
            ├── util/
            │   ├── PasswordUtilTest.java    5 tests
            │   ├── SessionManagerTest       6 tests
            │   ├── ValidatorTest.java       5 tests
            │   └── FlinkClientTest.java     4 tests   (Phase 7.2 — embedded HttpServer)
            └── widget/
                ├── SparklineTest.java       3 tests   (Phase 7.2)
                ├── PulseEffectTest.java     2 tests   (Phase 7.2)
                └── VietnamMapTest.java      6 tests   (Phase 7.3)
```

**Tổng: 77 @Test methods** (Phase 5 baseline 62 + Phase 7.2/7.3 +15 widget & Flink-client). ≥10 yêu cầu môn Java.

---

## 🛣️ Phase 5 roadmap

| Sub-phase | Status | Mô tả | Commit |
|-----------|--------|-------|--------|
| 5.0 | ✅ | Maven module + javafx-maven-plugin + folder structure | `62c824c` |
| 5.1 | ✅ | Login screen + BCrypt + SessionManager + UserDao | `08a97bd` |
| 5.2 | ✅ | Dashboard shell + TabPane 4 pillar + Security Score gauge | `de7c4dd` |
| 5.3 | ✅ | Region CRUD + Validator strategy | `7ced8e0` |
| 5.4 | ✅ | AlertRule CRUD + User CRUD (admin-only) | `8caec9e` |
| 5.5 | ✅ | Test consolidation + final docs | (file này) |

---

## 🔗 Liên kết

- Parent plan: [`UPGRADE_PLAN.md §24.5`](../UPGRADE_PLAN.md)
- Schema: [`infra/script/`](../infra/script/) — 11 tables + 19 views có sẵn từ Phase 0-2.6
- Progress log: [`docs/PROGRESS.md`](../docs/PROGRESS.md)
