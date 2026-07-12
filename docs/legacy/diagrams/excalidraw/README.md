# 🎨 Excalidraw Architecture Diagrams — Java Backend

Hand-editable architecture diagrams for the **VES-Monitor** real-time fuel-price platform (Kafka + Flink + PostgreSQL + Spring Boot). All five `.excalidraw` files in this folder are plain JSON — open them in [excalidraw.com](https://excalidraw.com) (File → Open) or with the VS Code / Cursor extension `pomdtr.excalidraw-editor`.

These are **complementary** to the Mermaid diagrams in `../*.md` — Excalidraw favours quick whiteboard-style edits, Mermaid favours diff-friendly source.

## Files

| # | File | Description |
|---|---|---|
| 1 | [`01_system_overview.excalidraw`](./01_system_overview.excalidraw) | End-to-end system: 4 mock generators → Kafka topics → Flink job → Postgres → Spring Boot REST API → Android / JavaFX / Postman / Metabase. |
| 2 | [`02_data_flow.excalidraw`](./02_data_flow.excalidraw) | Detailed real-time pipeline for the fuel-prices stream: parse → keyBy → 4 parallel branches (raw sink, 1-min window, price-change detector, rule-based detector) → JDBC sinks → recommendations. |
| 3 | [`03_database_schema.excalidraw`](./03_database_schema.excalidraw) | All 13 Postgres tables grouped by domain (Auth, Reference, Streaming raw, Aggregation, Alerts, Recommendations) with the key FK relationships. |
| 4 | [`04_api_layer.excalidraw`](./04_api_layer.excalidraw) | Spring Boot stack: Clients → Security FilterChain → 7 Controllers (13 endpoints) → 5 DAOs (JdbcTemplate) → PostgreSQL. |
| 5 | [`05_deployment.excalidraw`](./05_deployment.excalidraw) | Docker Compose Lite-profile topology: containers, ports, memory budgets (~2.85 GB total), volume mounts, network bridge. |

## How to open / edit

### Web (no install)

1. Go to <https://excalidraw.com>
2. Menu → **File → Open** → pick any `.excalidraw` file from this folder.
3. Edit, then **File → Save As** to overwrite the same file.

### VS Code / Cursor extension

1. Install [`pomdtr.excalidraw-editor`](https://marketplace.visualstudio.com/items?itemName=pomdtr.excalidraw-editor).
2. Click any `.excalidraw` file — it renders inline.
3. Edits save back to the file automatically.

### Export to PNG / SVG

Inside Excalidraw: menu → **Export image…** → PNG / SVG. Recommended `2x` scale for slides.

## Style key

- **Blue** — ingest / Kafka / data sources
- **Purple** — Flink / processing
- **Teal** — PostgreSQL / storage
- **Orange** — Spring Boot REST API
- **Gray (dashed)** — optional / external / cross-cutting
- **Pink** — filtering / decision
- **Rounded boxes** — services / tables; **dashed boxes** — optional or external boundaries.

## Caveats

These diagrams are **best-effort accurate** as of commit `0859e12` on `origin/main`. The source of truth is always the code:

- Pipeline & sinks → `flink-jobs/fuel-flink-job/src/main/java/org/cloud/KafkaConsumerApplication.java`
- Schema → `infra/script/01_*` … `09_*.sql`
- API endpoints → `backend-api/src/main/java/vn/edu/ves/api/controller/*Controller.java`
- Deployment → `infra/docker-compose.yml` (+ `docker-compose.bi.yml` overlay)

If the code drifts, please re-edit these `.excalidraw` files directly — they are the artefact, not generated.
