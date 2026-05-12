# 🖥️ VES Admin Desktop (JavaFX)

> **Phase 5** của UPGRADE_PLAN §24.5 — JavaFX 17 admin desktop, 3-layer, JDBC trực tiếp Postgres.

[![Status](https://img.shields.io/badge/status-Phase%205.0%20foundation-yellow)]() [![Java](https://img.shields.io/badge/Java-11-orange)]() [![JavaFX](https://img.shields.io/badge/JavaFX-17.0.10%20LTS-purple)]()

---

## 🎯 Mục tiêu

5 màn theo §24.5:

1. **Login** — BCrypt verify seed users (admin/manager/viewer từ Phase 2)
2. **Dashboard** — Security Score gauge + TabPane 4 pillar (đọc 4 view có sẵn)
3. **Region CRUD** — quản lý 6 region
4. **AlertRule CRUD** — quản lý threshold cho 4 pillar
5. **User CRUD** — admin-only, quản lý user system

3-layer pattern: **Controller (FXML) → Service → DAO → JDBC**.

Design patterns checkpoint môn Java:
- **Singleton**: `DatabaseConfig`, `SessionManager`
- **DAO**: `UserDao`, `RegionDao`, `AlertRuleDao`, `ViewsDao`
- **MVC**: FXML view + Controller class + Model class
- **Builder** (Phase 5.3+): filter builder cho alert search
- **Strategy** (Phase 5.5): Validator strategies (NumericRange, NotBlank, Pattern, ...)

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

> 📌 **Phase 5.0**: chạy ra cửa sổ **placeholder gradient blue** với text "VES-Monitor Admin Desktop / Phase 5.0 — Foundation OK". Login form thật sẽ wire ở Phase 5.1.

### Override DB connection

```bash
# Env vars
DB_URL=jdbc:postgresql://192.168.1.100:5432/fuel_db DB_USER=demo DB_PASSWORD=xxx \
    mvn -pl desktop-admin javafx:run

# System properties
mvn -pl desktop-admin javafx:run \
    -Ddb.url=jdbc:postgresql://localhost:5432/test_db
```

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
    │   │   ├── MainApp.java                Application entry
    │   │   ├── controller/                 FXML controllers (Phase 5.1+)
    │   │   ├── service/                    Business logic (Phase 5.1+)
    │   │   ├── dao/                        JDBC data access (Phase 5.1+)
    │   │   ├── model/                      POJOs (Phase 5.1+)
    │   │   └── util/
    │   │       └── DatabaseConfig.java     Singleton config + Connection factory
    │   └── resources/
    │       ├── application.properties      DB defaults
    │       ├── logback.xml                 Logging
    │       ├── fxml/login.fxml             Placeholder (Phase 5.1 sẽ replace)
    │       └── css/material.css            Material-inspired theme
    └── test/
        └── java/vn/edu/ves/desktop/        JUnit 4 tests (Phase 5.5: ≥10 test)
```

---

## 🛣️ Phase 5 roadmap

| Sub-phase | Done | Mô tả |
|-----------|------|-------|
| 5.0 | ✅ | Maven module + javafx-maven-plugin + folder structure + smoke `mvn javafx:run` |
| 5.1 | ⏳ | Login screen + BCrypt + SessionManager |
| 5.2 | ⏳ | Dashboard shell + TabPane 4 pillar + Security Score gauge |
| 5.3 | ⏳ | Region CRUD |
| 5.4 | ⏳ | AlertRule CRUD + User CRUD |
| 5.5 | ⏳ | JUnit ≥10 test |

---

## ⚠️ Build pending notice

Phase 5.0 code-complete, lint clean. **Build runtime test pending** do parent project trên máy Bosch laptop bị NTLM proxy block khi pull JavaFX deps. Khi có hotspot 4G hoặc cntlm bridge:

```bash
mvn -pl desktop-admin -am clean compile     # ~30s pull JavaFX 17 deps
mvn -pl desktop-admin javafx:run            # mở cửa sổ
```

Nếu sau khi compile gặp issue `JavaFX runtime not found`:
- javafx-maven-plugin 0.0.8 cần Java 11+. Verify `java --version`.
- Trên Windows, plugin sẽ tự config module-path. Nếu fail, fallback chạy bằng IDE (IntelliJ → Run Configuration với mainClass = `vn.edu.ves.desktop.MainApp` + VM options `--module-path /path/to/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml`).

---

## 🔗 Liên kết

- Parent plan: [`UPGRADE_PLAN.md §24.5`](../UPGRADE_PLAN.md)
- Schema: [`infra/script/`](../infra/script/) — 11 tables + 19 views có sẵn từ Phase 0-2.6
- Progress log: [`docs/PROGRESS.md`](../docs/PROGRESS.md)
