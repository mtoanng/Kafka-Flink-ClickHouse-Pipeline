# 📦 Submission Package — VES-Monitor v1.0.0

> **Một-trang manifest** liệt kê mọi artifact mà grader / judge / reviewer cần để chấm bài hoặc reproduce kết quả. Mọi link đều dùng đường dẫn tương đối — sẽ tự work khi clone repo về local hoặc browse trên GitHub.
>
> **Suggested review order** for graders: A → B → C → D → E (Source → Docs → Diagrams → Slides/Demo → Verification).

---

## A. Source code & release

| Artifact | Reference |
|---|---|
| **Repository** | https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres |
| **Tag** | `v1.0.0` |
| **Tag commit** | `3d30b39` (tag points here — snapshot at submission time) |
| **Current `main` HEAD** | `e64d447` (Phase 7.6 backend pillar migration — post-tag fix; see [`RELEASE_NOTES.md`](./RELEASE_NOTES.md) → "Post-release fixes") |
| **Branch** | `main` |
| **Release date** | 13 May 2026 |
| **Release notes** | [`docs/RELEASE_NOTES.md`](./RELEASE_NOTES.md) |
| **License** | Educational / academic — IS402.P21 final project |

**Clone & checkout `v1.0.0`**:
```powershell
git clone --branch v1.0.0 https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres.git
cd Real-time-processing-with-Kafka-Flink-Postgres
```

**Build verification**:
```powershell
mvn -q clean package -DskipTests           # ~60s warm cache
mvn -pl desktop-admin test                 # expect 77/77 PASS in ~18s
```

---

## B. Top-level documentation

| # | Document | Purpose | Audience |
|---|---|---|---|
| 1 | [`README.md`](../README.md) | Project introduction · architecture · quickstart · stack table · API ref · phase history · features highlight | Anyone (first contact) |
| 2 | [`UPGRADE_PLAN.md`](../UPGRADE_PLAN.md) | Original 7-phase execution plan (§24 = minimal-execution roadmap) | Reviewer of methodology |
| 3 | [`JAVA_FINAL_PROJECT_REQUIREMENT.md`](../JAVA_FINAL_PROJECT_REQUIREMENT.md) | Course requirements (must satisfy) | Course instructor |
| 4 | [`docs/PROGRESS.md`](./PROGRESS.md) | Phase-by-phase log + verification snapshots (sole source of truth for "what happened when") | Technical reviewer |
| 5 | [`docs/DEMO_RUN_LOG.md`](./DEMO_RUN_LOG.md) | Live E2E runtime verification (full stack snapshot on `HEAD = 30265f1`) | Reviewer wanting proof |
| 6 | [`docs/SUBMISSION_PACKAGE.md`](./SUBMISSION_PACKAGE.md) | **This file** — grader manifest | Grader (first stop) |
| 7 | [`docs/RELEASE_NOTES.md`](./RELEASE_NOTES.md) | v1.0.0 highlights · phase summary · known issues · migration · acknowledgments | Anyone wanting "what's in this release?" |

---

## C. Architecture diagrams

Folder: [`docs/diagrams/`](./diagrams/) · 4 Mermaid diagrams + index README. Renders inline on GitHub.

| # | File | Type | Renders |
|---|---|---|---|
| 1 | [`01_dataflow.md`](./diagrams/01_dataflow.md) | `flowchart LR` | End-to-end pipeline 5 tier + throughput numbers |
| 2 | [`02_erd.md`](./diagrams/02_erd.md) | `erDiagram` + view catalogue | 13 tables + FK + 17 views |
| 3 | [`03_class_desktop.md`](./diagrams/03_class_desktop.md) | `classDiagram` | 3-layer JavaFX architecture + 10 design patterns |
| 4 | [`04_pillar_framework.md`](./diagrams/04_pillar_framework.md) | `flowchart TD` | IEA/APERC taxonomy → 4 pillars → 16 sub-indicators + formulas |
| 5 | [`README.md`](./diagrams/README.md) | (index) | Preview snippets · `mmdc` export instructions |

**Export to PNG / SVG** for slide deck:
```bash
npm install -g @mermaid-js/mermaid-cli
mmdc -i docs/diagrams/01_dataflow.md         -o docs/diagrams/01_dataflow.png         -w 1920 -H 1080 --scale 2
mmdc -i docs/diagrams/02_erd.md              -o docs/diagrams/02_erd.png              -w 1920 -H 1080 --scale 2
mmdc -i docs/diagrams/03_class_desktop.md    -o docs/diagrams/03_class_desktop.png    -w 1920 -H 1080 --scale 2
mmdc -i docs/diagrams/04_pillar_framework.md -o docs/diagrams/04_pillar_framework.png -w 1920 -H 1080 --scale 2
```

---

## D. Presentation pack

| # | Artifact | Purpose |
|---|---|---|
| 1 | [`docs/SLIDES_OUTLINE.md`](./SLIDES_OUTLINE.md) | 15-slide presenter outline (~18 min); per-slide heading + 3-5 bullets + speaker notes |
| 2 | [`docs/DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) | 18-minute copy-paste demo runbook with T+0 → T+18 timing · backup commands · Q&A prep |

**Recommended deck format**: Google Slides or PowerPoint 16:9, Inter / Roboto / SF Pro font, IEA accent blue `#1976d2`. Embed 4 diagram PNGs at slide 5 (data flow), slide 7 (pillar framework), slide 11 (ERD), slide 13 (class diagram).

**Speaker tip**: rehearse with the timer in [`SLIDES_OUTLINE.md`](./SLIDES_OUTLINE.md) → "Phụ lục cho presenter" section.

---

## E. API contract & test collection

| # | Artifact | Reference |
|---|---|---|
| 1 | OpenAPI 3 spec (raw JSON) | [`docs/openapi.json`](./openapi.json) |
| 2 | Postman collection | [`docs/VES-Monitor.postman_collection.json`](./VES-Monitor.postman_collection.json) |
| 3 | Swagger UI (running) | http://localhost:8090/swagger-ui.html |
| 4 | REST API summary table | [`README.md`](../README.md) → "API quick reference" section (13 endpoints) |
| 5 | Backend README (build/run/proxy) | [`backend-api/README.md`](../backend-api/README.md) |

**Import Postman collection**: `File → Import → Upload File → docs/VES-Monitor.postman_collection.json`. The `Login` request has a pre-request script that auto-populates the `{{token}}` collection variable for downstream requests.

**Auth seed users** (Phase 2 seed in `infra/script/04_seed_basic.sql`):

| Username | Password | Role |
|---|---|---|
| `admin` | `admin` | ADMIN |
| `manager` | `manager` | MANAGER |
| `user` | `user` | VIEWER |

---

## F. Live runtime verification

| Artifact | Reference |
|---|---|
| **E2E demo run log** | [`docs/DEMO_RUN_LOG.md`](./DEMO_RUN_LOG.md) |
| **Phase log with snapshots** | [`docs/PROGRESS.md`](./PROGRESS.md) — see Phase 7.5 "Final QA pass" + Phase 7.6 "Backend pillar migration" sections |
| **Verified ESI snapshot** | `overall=72.82 ELEVATED` (P1=64.83, P2=59.59, P3=90.11, P4=72.09) on `HEAD = 30265f1` |
| **Verified data volume** | `fuel_prices_raw=26 930`, `grid_load_raw=5 275`, `renewable_output_raw=7 767`, `emission_raw=872`, `alerts=234`, `recommendations=8` |
| **Verified Flink job state** | 1 job RUNNING, 18 / 18 vertices RUNNING |
| **Verified REST smoke (tag `v1.0.0` @ `3d30b39`)** | 8/13 endpoints `200 OK` — 5 endpoints `500` (legacy-view regression, see [`RELEASE_NOTES.md`](./RELEASE_NOTES.md) → "Known issues") |
| **Verified REST smoke (current `main` @ `e64d447`)** | ✅ **13/13 endpoints `200 OK`** — Phase 7.6 backend pillar migration resolved at `e64d447`; 11 new `@WebMvcTest` smoke tests in `backend-api`. See [`RELEASE_NOTES.md`](./RELEASE_NOTES.md) → "Post-release fixes". |

### Post-release fixes (post-tag, on `main`)

| # | Fix | Commit | Status | Notes |
|---|---|---|---|---|
| 1 | **Phase 7.6 — Backend pillar migration** | `e64d447` | ✅ Resolved | Closes the 5-endpoint backend regression flagged by Phase 7.5 QA. `PillarDao` + `SecurityDao` rewritten against the IEA/APERC views; 4 pillar DTOs reshaped (`Pillar{1..4}{SupplySecurity,MarketResilience,GridReliability,EnergyTransition}Dto`). Legacy paths (`/api/pillars/{1..4}/{outlook,volatility,shedding,netzero}`) kept as backward-compat aliases for the new canonical paths (`/api/pillars/{1..4}/{supply-security,market-resilience,grid-reliability,energy-transition}`). 13 / 13 REST smoke green; 11 new `@WebMvcTest` smoke tests pass. `docs/openapi.json` (20 paths) + `docs/VES-Monitor.postman_collection.json` (8 folders / 19 requests) regenerated. |

> Tag `v1.0.0` is **intentionally not re-tagged**; it remains a snapshot at `3d30b39`. To reproduce the fixed state, check out `origin/main` instead of the tag.

---

## G. Test artifacts

| Module | Test command | Expected | Test report folder |
|---|---|---:|---|
| `desktop-admin` | `mvn -pl desktop-admin test` | **77 / 77 PASS** in ~18 s | `desktop-admin/target/surefire-reports/` |
| `backend-api` (current `main` @ `e64d447`) | `mvn -pl backend-api -am clean test` | **11 / 11 PASS** — `@WebMvcTest` smoke (8 pillar + 3 security; Phase 7.6 resolved) | `backend-api/target/surefire-reports/` |
| All modules | `mvn clean test` | All builds GREEN | per-module folders |

**Test breakdown** (`desktop-admin/`):
- 24 DAO integration tests (H2 in-memory `MODE=PostgreSQL`)
- 22 service unit tests (Mockito mock DAO)
- 16 utility unit tests (PasswordUtil, SessionManager, Validator)
- 15 widget / Flink client tests (embedded `com.sun.net.httpserver.HttpServer`)

---

## H. Phase history & milestones

[`docs/PROGRESS.md`](./PROGRESS.md) is the authoritative phase log. Quick links to the most important sections:

| Section | Reference |
|---|---|
| Phase tracking table (top of file) | [`PROGRESS.md` lines 1-32](./PROGRESS.md) |
| Phase 4 — multi-pillar Flink job | [`PROGRESS.md` Phase 4 section](./PROGRESS.md) |
| Phase 4.5 — Spring Boot REST API | [`PROGRESS.md` Phase 4.5 section](./PROGRESS.md) |
| Phase 5.x — JavaFX desktop 5 screens | [`PROGRESS.md` Phase 5.0-5.5 sections](./PROGRESS.md) |
| Phase 7.1 — IEA/APERC redesign | [`PROGRESS.md` Phase 7.1 section](./PROGRESS.md) |
| Phase 7.2 — Real-time visibility | [`PROGRESS.md` Phase 7.2 section](./PROGRESS.md) |
| Phase 7.3 — Interactive maps | [`PROGRESS.md` Phase 7.3 section](./PROGRESS.md) |
| Phase 7.5 — Final QA pass | [`PROGRESS.md` Phase 7.5 section](./PROGRESS.md) |
| E2E live verification | [`PROGRESS.md` "E2E runtime verified" section](./PROGRESS.md) |

---

## I. Team & coordination

| Artifact | Reference |
|---|---|
| 5-person team work breakdown structure (B/C/D/E roles × 5 PR steps each) | [`docs/TEAM_TASKS.md`](./TEAM_TASKS.md) |
| Android sibling project onboarding | [`docs/ANDROID_ONBOARDING.md`](./ANDROID_ONBOARDING.md) |
| Android sibling repo | https://github.com/mtoanng/DataStream |
| Phase-by-phase ownership | See [`PROGRESS.md`](./PROGRESS.md) commit log + author |

---

## J. Environment & build setup

| Artifact | Reference |
|---|---|
| **Bosch NTLM proxy workaround** | [`docs/PROXY_SETUP.md`](./PROXY_SETUP.md) |
| **Portable Maven 3.9.6 install** | [`docs/MAVEN_SETUP.md`](./MAVEN_SETUP.md) |
| **Docker Compose stack** | [`infra/docker-compose.yml`](../infra/docker-compose.yml) |
| **SQL init scripts** (loaded in order on first Postgres boot) | [`infra/script/`](../infra/script/) |
| **Docker run scripts** | [`scripts/run.sh`](../scripts/run.sh) · [`scripts/run.ps1`](../scripts/run.ps1) |
| **Healthcheck** | [`scripts/healthcheck.sh`](../scripts/healthcheck.sh) |
| **AI usage log** | [`docs/AI_USAGE_LOG.md`](./AI_USAGE_LOG.md) |

---

## K. Grading rubric mapping (môn Java IS402.P21)

| Course requirement | Where to find it |
|---|---|
| ≥ 10 design patterns demonstrated | [`README.md`](../README.md) "Design patterns inventory" · [`docs/diagrams/03_class_desktop.md`](./diagrams/03_class_desktop.md) "Design patterns inventory" table (10 patterns) |
| 3-layer architecture | [`docs/diagrams/03_class_desktop.md`](./diagrams/03_class_desktop.md) class diagram + "Layer responsibility matrix" |
| MVC pattern | Controller × FXML × Model split in `desktop-admin/src/main/java/vn/edu/ves/desktop/{controller,model}/` + `desktop-admin/src/main/resources/fxml/` |
| ≥ 10 JUnit test methods | **77 tests** in `desktop-admin/` (`mvn -pl desktop-admin test`) |
| JDBC direct (no ORM magic) | `desktop-admin/.../dao/BaseDao.java` + 4 DAO subclasses · `backend-api/.../dao/*.java` (JdbcTemplate) |
| Database integration | 13 tables + 17 views in `fuel_prices` Postgres DB · all schema in [`infra/script/`](../infra/script/) |
| JavaFX desktop application | `desktop-admin/` module · 5 screens · ~ 4 500 LOC Java + ~ 600 LOC FXML/CSS |
| Distributed / scalable component | Kafka + Flink + Postgres pipeline · 4 generator + 1 Flink job + 5 docker service |
| Authentication & authorization | BCrypt password hashing · JWT HS256 stateless · 3 role (ADMIN / MANAGER / VIEWER) · `SecurityConfig.java` |
| Modular Maven multi-module build | Parent `pom.xml` + 4 child modules — `mvn clean package` builds all in one |
| Documentation | This `SUBMISSION_PACKAGE.md` + 7 docs in `docs/` + 4 module README files |

---

## L. Quick links cheat-sheet

| Want to ... | Open |
|---|---|
| Understand the project in 5 min | [`README.md`](../README.md) |
| See the live data flow | [`docs/diagrams/01_dataflow.md`](./diagrams/01_dataflow.md) |
| Inspect database schema | [`docs/diagrams/02_erd.md`](./diagrams/02_erd.md) |
| Review Java code architecture | [`docs/diagrams/03_class_desktop.md`](./diagrams/03_class_desktop.md) |
| Verify pillar formula compliance | [`docs/diagrams/04_pillar_framework.md`](./diagrams/04_pillar_framework.md) · [`infra/script/08_pillars_v2.sql`](../infra/script/08_pillars_v2.sql) |
| Prepare a slide deck | [`docs/SLIDES_OUTLINE.md`](./SLIDES_OUTLINE.md) |
| Run the live demo | [`docs/DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) |
| Test the REST API | [`docs/VES-Monitor.postman_collection.json`](./VES-Monitor.postman_collection.json) |
| Reproduce the verified snapshot | [`docs/DEMO_RUN_LOG.md`](./DEMO_RUN_LOG.md) + [`docs/RELEASE_NOTES.md`](./RELEASE_NOTES.md) → "Build & run reproducibility" |
| Read the phase history | [`docs/PROGRESS.md`](./PROGRESS.md) |
| See the v1.0.0 release notes | [`docs/RELEASE_NOTES.md`](./RELEASE_NOTES.md) |

---

> **Submission ready** — `v1.0.0` tagged at `3d30b39` on `origin/main`, 13 May 2026. Current `main` HEAD is `e64d447` with the Phase 7.6 backend pillar migration applied (see § F → "Post-release fixes" and [`RELEASE_NOTES.md`](./RELEASE_NOTES.md) → "Post-release fixes"). Tag is preserved as-is — no re-tag was issued.
