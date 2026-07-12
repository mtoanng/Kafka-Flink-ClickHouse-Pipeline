# 🛣 FUTURE_WORK — Optional improvements after handover

> **Status**: All items below are **OPTIONAL**. The repo at tag `v1.0.0-handover` is fully MVP-ready for IS402.P21 course submission. Nothing in this document is a blocker.
>
> **Audience**: any team member who has spare cycles after the main course submission and wants to polish the codebase further.
>
> **Source**: items here are lifted from the prior audit reports (`docs/MASS_QA_REPORT_JAVA.md` § 8, `docs/AUDIT_REPORT.md`, `docs/MASS_QA_REPORT_JAVA.md` § 4.2/4.3) plus a few obvious "would be nice" features. Each is sized to fit comfortably in a single session.
>
> **Conventions**:
> - Effort is wall-clock for a developer who already knows the codebase.
> - "DB migration: yes" → adds an `infra/script/NN_*.sql` and a coordinated DAO change.
> - "Risk" is a 1-5 estimate of how easily the change could go wrong.

---

## 🟢 Tier 1 — Quick wins (≤ 1 hour each)

These are the 5 items the prior `MASS_QA_REPORT_JAVA.md § 8` already flagged. Lifting them here so they're discoverable without re-reading the audit.

### 1. Tighten `FlinkClient` JSON parsing (~30 min)

- **What**: Replace 3 regex `Pattern.compile(...)` calls in `desktop-admin/src/main/java/vn/edu/ves/desktop/util/FlinkClient.java` with `com.fasterxml.jackson.databind.ObjectMapper.readTree(...)`. Jackson is already on the classpath via SLF4J/Spring transitives.
- **Why**: Catches schema drift in Flink REST API across versions (the API has changed between 1.13 → 1.17 already). Improves "live ticker" reliability under load.
- **Files**: `desktop-admin/src/main/java/vn/edu/ves/desktop/util/FlinkClient.java`.
- **DB migration**: no.
- **Risk**: 2 / 5 — single class, 5 unit tests in `FlinkClientTest.java` will catch any regression.

### 2. Tune Flink JDBC sink batch size (~40 min)

- **What**: Change `flink-jobs/fuel-flink-job/src/main/java/.../JdbcPostgresSink.java`'s `JdbcExecutionOptions.builder().withBatchSize(1)` to `withBatchSize(100).withBatchIntervalMs(500)`.
- **Why**: At observed ~1000 ev/s aggregate throughput, this drops Postgres write-amplification by ~100× without losing more than 0.5 s of data on a crash (Flink replays from Kafka).
- **Files**: `flink-jobs/fuel-flink-job/src/main/java/.../JdbcPostgresSink.java` (the `createJdbcSink()` factory method).
- **DB migration**: no.
- **Risk**: 2 / 5 — must validate with a load test (`scripts/phase4_stress_alerts.sh`) that batched writes don't break alert latency expectations.

### 3. Add a single GitHub Actions CI workflow (~20 min)

- **What**: Create `.github/workflows/ci.yml` running `mvn -pl desktop-admin -am test` + `mvn -pl backend-api -am test` on every push and PR to `main`.
- **Why**: Locks in the 88/88 test baseline against future drift. Gives graders / reviewers a green-checkmark badge to point at.
- **Files**: new `.github/workflows/ci.yml` (~30 lines).
- **DB migration**: no.
- **Risk**: 1 / 5 — additive, no impact on local development.

### 4. Extract `addRawSink()` helper in `KafkaConsumerApplication` (~45 min)

- **What**: Refactor the 4 nearly-identical raw-sink stanzas (`fuel_prices_raw`, `grid_load_raw`, `renewable_output_raw`, `emission_raw`) in `KafkaConsumerApplication.main()` into a single generic `addRawSink(stream, tableName, columns, ps -> ...)` helper. Same pattern as the existing `addAlertSinks()`.
- **Why**: Drops `main()` length from ~270 lines to ~150 lines and makes each Kafka topic's wiring a single 4-line block.
- **Files**: `flink-jobs/fuel-flink-job/src/main/java/.../KafkaConsumerApplication.java`.
- **DB migration**: no.
- **Risk**: 3 / 5 — touches the wiring point of 7 parallel streams; verify with a full pipeline run (`scripts/run.sh` then `scripts/healthcheck.sh`).

### 5. Add SLF4J MDC + `X-Correlation-Id` header to backend (~1 hour)

- **What**: Add an `OncePerRequestFilter` that reads `X-Correlation-Id` (or generates a UUID), pushes onto `MDC.put("corrId", ...)`, removes on filter exit. Update Logback pattern to include `%X{corrId}` on every line.
- **Why**: Foundation for any multi-instance deploy (Phase 8 Cloudflare Tunnel). Lets you trace a single user action across the full Spring Boot log.
- **Files**: new `backend-api/src/main/java/vn/edu/ves/api/config/CorrelationIdFilter.java`, edit `backend-api/src/main/resources/application.yml` (logback pattern), edit `SecurityConfig.java` (register filter).
- **DB migration**: no.
- **Risk**: 2 / 5 — additive, doesn't change response shape; tests still pass.

---

## 🟡 Tier 2 — Nice-to-have features (≤ 4 hours each)

Add-on features that *enhance* the project but aren't required by the rubric. The system ships fine without any of them.

### 6. AI integration — TF Lite anomaly detection for fuel prices (~4 hours)

- **What**: Add a Flink `CoProcessFunction` that runs a tiny TF Lite model on each `fuel-prices` event and emits an `anomaly_score` to a new `fuel_price_anomalies` Postgres table. Surface in the JavaFX desktop's "Pillar 2" tab as a small chart.
- **Why**: Differentiates the project from a pure rule-based alerting system. Demonstrates ML deployment in a streaming pipeline (a common interview question).
- **Files**: new `flink-jobs/fuel-flink-job/src/main/java/.../FuelPriceAnomalyDetector.java`, new `flink-jobs/fuel-flink-job/src/main/resources/models/fuel-price-iforest.tflite`, new `infra/script/10_init_anomaly.sql`, edit `desktop-admin/.../controller/DashboardController.java` (Pillar 2 tab).
- **DB migration**: yes (`10_init_anomaly.sql`).
- **Risk**: 3 / 5 — adds a 1-2 MB native dep; verify it builds in the existing Docker-Lite stack.

### 7. WebSocket push for critical alerts (~3 hours)

- **What**: Add Spring WebSocket + STOMP endpoint at `/ws/alerts`. JavaFX desktop opens a STOMP connection in `LiveMetricsServiceImpl` and pushes a slide-in toast for each `severity=CRITICAL` alert (replaces the current 5 s polling).
- **Why**: Drops alert-to-screen latency from ~5 s (polling) to ~50 ms (push). Better demo experience.
- **Files**: new `backend-api/.../config/WebSocketConfig.java`, new `backend-api/.../controller/AlertWebSocketController.java`, edit `desktop-admin/.../service/LiveMetricsServiceImpl.java`.
- **DB migration**: no.
- **Risk**: 3 / 5 — STOMP behind a corporate proxy can be tricky; have a fallback to polling.

### 8. Multi-tenant region selection (~3 hours)

- **What**: Today the schema seeds `region_code IN ('VN_NORTH','VN_CENTRAL','VN_SOUTH','VN_HANOI','VN_HCMC','VN_DANANG')`. Generalise to allow any ISO-3166-2 region. Add a region-picker dropdown to JavaFX desktop and an `?region=` query param to `/api/pillars/*`.
- **Why**: Lets the same deploy serve multiple countries (Vietnam → ASEAN). Useful if the project is reused for a different course.
- **Files**: edit `infra/script/02_init_users_regions.sql`, edit `RegionDao.java`, edit `PillarController.java` (add `@RequestParam`), edit `desktop-admin/.../controller/RegionController.java`.
- **DB migration**: yes (region seed becomes ASEAN).
- **Risk**: 3 / 5 — must keep backward-compat for existing `VN_*` codes.

### 9. Metabase auto-config script (~2 hours)

- **What**: Today `infra/docker-compose.bi.yml` brings up Metabase but leaves dashboard creation manual. Add `scripts/setup-metabase.sh` that calls Metabase's `/api/setup` + `/api/dashboard` to provision the 4 pillar dashboards programmatically from a JSON definition.
- **Why**: Makes the optional BI overlay reproducible. Good for graders who want to see the BI side.
- **Files**: new `scripts/setup-metabase.sh`, new `infra/metabase/dashboards/{p1,p2,p3,p4}.json`.
- **DB migration**: no (Metabase has its own H2 DB).
- **Risk**: 2 / 5 — Metabase API is stable; fail-safely if Metabase isn't running.

### 10. API rate limiting per user (~2 hours)

- **What**: Add `@EnableMethodSecurity` + a Bucket4j-based interceptor that enforces 60 req/min per JWT subject on `/api/**`. Returns `429 Too Many Requests` with `Retry-After` header on overflow.
- **Why**: Defends against accidental client-side runaways (e.g., Android polling bug). Demonstrates a production-grade middleware pattern.
- **Files**: edit `backend-api/pom.xml` (add `bucket4j-core`), new `backend-api/.../config/RateLimitInterceptor.java`, edit `SecurityConfig.java`.
- **DB migration**: no.
- **Risk**: 2 / 5 — additive; ensure Android client handles 429 gracefully.

---

## 🔵 Tier 3 — Polish items (≤ 2 hours each)

Cosmetic / quality-of-life changes. Lowest priority.

### 11. Add API versioning header (`/api/v1/...`) (~1.5 hours)

- **What**: Move all REST controllers under `@RequestMapping("/api/v1")`. Keep `/api/*` as a compatibility shim that 301-redirects to `/api/v1/*` for one release cycle.
- **Why**: Standard REST hygiene. Makes future breaking changes cleaner (`/api/v2/...`).
- **Files**: 7 controllers in `backend-api/.../controller/`, OpenAPI metadata.
- **DB migration**: no.
- **Risk**: 2 / 5 — Android sibling repo must update its `BASE_URL`.

### 12. OpenAPI spec auto-publish to GitHub Pages (~1 hour)

- **What**: Add a GitHub Action that on tag push generates `openapi.json` (via `mvn -pl backend-api spring-boot:run` + `curl /v3/api-docs`), then publishes a static Swagger UI under `gh-pages` branch. Result: persistent URL `https://mtoanng.github.io/Real-time-processing-with-Kafka-Flink-Postgres/`.
- **Why**: Reviewers can browse the API without running the backend. Looks professional.
- **Files**: new `.github/workflows/publish-openapi.yml`.
- **DB migration**: no.
- **Risk**: 1 / 5 — fully additive.

### 13. Spring Boot Actuator (~1 hour)

- **What**: Add `spring-boot-starter-actuator` and expose `/actuator/health` (deep), `/actuator/metrics`, `/actuator/info`. Keep `/api/health` as the public alias used by Cloudflare Tunnel probes.
- **Why**: Standard observability surface. Lets the deployment pipeline scrape JVM + DB pool metrics with a Prometheus exporter later.
- **Files**: edit `backend-api/pom.xml`, edit `application.yml` (`management.endpoints.web.exposure.include`).
- **DB migration**: no.
- **Risk**: 2 / 5 — endpoints are public-by-default; lock down via `management.endpoint.health.show-details=when-authorized`.

### 14. Logback structured JSON logging (~1.5 hours)

- **What**: Replace the current pattern layout in `backend-api/src/main/resources/logback-spring.xml` (or `application.yml` `logging.pattern.*`) with `LogstashEncoder` from `net.logstash.logback:logstash-logback-encoder`. Output one JSON object per log line.
- **Why**: Makes logs grep-able / parsable by any log aggregator (Loki, ELK, Datadog).
- **Files**: edit `backend-api/pom.xml`, new `backend-api/src/main/resources/logback-spring.xml`.
- **DB migration**: no.
- **Risk**: 2 / 5 — local dev users may dislike one-line JSON; provide a `application-dev.yml` profile that keeps human-readable output.

### 15. RFC 7807 problem+json error responses (~2 hours)

- **What**: Update `GlobalExceptionHandler.java` to return `application/problem+json` per RFC 7807 (`type`, `title`, `status`, `detail`, `instance`). Spring 6 has `ProblemDetail` built-in but the project is on Spring Boot 2.7 — use a custom DTO.
- **Why**: Clients (Android, Postman) get a standardised error shape that's easier to handle. Future-proofs the migration to Spring Boot 3.x.
- **Files**: edit `backend-api/.../exception/GlobalExceptionHandler.java`, new `backend-api/.../dto/ProblemDetailDto.java`.
- **DB migration**: no.
- **Risk**: 3 / 5 — Android `ErrorResponse.kt` parser may need a fallback for the new `type`/`title` fields.

### 16. Remove duplicate SQL init script numbering (~30 min)

- **What**: Today `infra/script/` has two files starting with `08_*`: `08_pillars_v2.sql` and `08_seed_security_features.sql`. Postgres runs init scripts in alphabetical order so the behaviour is stable, but the duplicate prefix is confusing. Renumber the seed file to `08a_seed_security_features.sql` (or move both to `08_*` / `09_*` with a clean break).
- **Why**: Makes the init order obvious to a new developer reading `infra/script/`. Documented as cosmetic finding C4 in `docs/MASS_QA_REPORT_JAVA.md`.
- **Files**: rename `infra/script/08_seed_security_features.sql`, edit any docs that reference the old name.
- **DB migration**: no (renaming an init script doesn't break an existing volume; only affects fresh `pgdata` boots).
- **Risk**: 1 / 5 — purely cosmetic.

---

## ❌ Explicitly NOT in this list

These were considered and deliberately rejected:

- **Spring Boot 3.x upgrade** — would require a Java 17 baseline (currently Java 11 on Flink modules), AGP-style migration on Spring Security, and is out of scope for an academic project.
- **Replace JdbcTemplate with JPA / Hibernate** — adds magic, no graded benefit, hurts the "JDBC direct" rubric line.
- **Replace Postgres with TimescaleDB** — would speed up time-series queries but requires reseeding all 13 tables; high effort + risk.
- **Replace Kafka with Pulsar / Redpanda** — Kafka is what's tested; swapping the broker for a "more modern" one has no graded benefit.
- **OAuth2 / Keycloak instead of HS256 JWT** — Phase 8 deploy item; tracked in the original `UPGRADE_PLAN.md`.

---

## Submitting an improvement

If you want to land any item above:

1. Check it's still relevant — re-read the linked `docs/MASS_QA_REPORT_JAVA.md` section.
2. Open a `chore:`, `feat:`, or `perf:` PR (see your team's Conventional Commits guide).
3. Run `mvn -pl desktop-admin test && mvn -pl backend-api test` — both must stay green.
4. Update this file: move the item to a new `## ✅ Done` section and write 1 sentence about what changed.

> Last updated: 15 May 2026 (final handover gate).
