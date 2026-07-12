# 📐 Architecture Diagrams Index

Four Mermaid diagrams covering the **VES-Monitor** project across data, schema, code, and domain layers. All diagrams render inline on GitHub — open any `.md` file in your browser to view.

| # | File | Type | Audience | Nodes |
|---|---|---|---|---:|
| 1 | [`01_dataflow.md`](./01_dataflow.md) | `flowchart LR` | Architects, ops, evaluators | 14 + 5 subgraphs |
| 2 | [`02_erd.md`](./02_erd.md) | `erDiagram` + view table | DB engineers, evaluators | 13 entities |
| 3 | [`03_class_desktop.md`](./03_class_desktop.md) | `classDiagram` | Java reviewers (môn Java) | 28 classes |
| 4 | [`04_pillar_framework.md`](./04_pillar_framework.md) | `flowchart TD` | Domain experts (IEA/APERC) | 21 nodes |

## Preview snippets

### 1. Data flow — `01_dataflow.md`

End-to-end streaming pipeline: **4 generator JVMs → 4 Kafka topics → 1 Flink job (18 vertices) → 13 Postgres tables + 17 views → 3 consumers** (JavaFX desktop, Spring Boot REST, Mock API for Android). Annotated with measured throughput (~1 000 events/sec aggregate at full load).

### 2. ERD — `02_erd.md`

13 tables with PK/FK relationships, generated columns (e.g. `stock_days`, `load_pct`), plus a textual catalogue of the 17 computed views grouped by purpose (helper / dashboard / IEA-APERC / composite).

### 3. Class diagram — `03_class_desktop.md`

3-layer architecture (Controller → Service → DAO) for `desktop-admin/`, plus the 10 design patterns inventoried (Singleton, DAO, MVC, Strategy, Composite, Factory, Service Layer, Observer, Adapter, Template Method).

### 4. Pillar framework — `04_pillar_framework.md`

The IEA / APERC Energy Security taxonomy: **1 composite ESI → 4 pillars → 16 sub-indicators**. Each sub-indicator documented with formula and reference standard (IEA, IMF, NERC, IEEE 1366, IPCC AR6, etc.).

## Rendering tips

### View on GitHub

Just open any `.md` file in your browser — GitHub renders Mermaid natively (since 2022).

### Render locally to PNG / SVG

Install the [Mermaid CLI](https://github.com/mermaid-js/mermaid-cli) once:

```bash
npm install -g @mermaid-js/mermaid-cli
```

Then export every diagram in this folder:

```bash
# PNG (presentations)
mmdc -i docs/diagrams/01_dataflow.md -o docs/diagrams/01_dataflow.png --theme default --backgroundColor white
mmdc -i docs/diagrams/02_erd.md      -o docs/diagrams/02_erd.png      --theme default --backgroundColor white
mmdc -i docs/diagrams/03_class_desktop.md -o docs/diagrams/03_class_desktop.png --theme default --backgroundColor white
mmdc -i docs/diagrams/04_pillar_framework.md -o docs/diagrams/04_pillar_framework.png --theme default --backgroundColor white

# SVG (high-DPI / print)
mmdc -i docs/diagrams/01_dataflow.md -o docs/diagrams/01_dataflow.svg --theme default
```

`mmdc` extracts every fenced ` ```mermaid ` block, so the markdown commentary stays untouched.

### Embed in slide deck

For Google Slides / PowerPoint, export to PNG (above), then insert as image. Recommended size: **1920×1080 PNG @ 2× retina**:

```bash
mmdc -i 01_dataflow.md -o 01_dataflow.png -w 1920 -H 1080 --scale 2
```

### Embed in Word / DOCX

VS Code extension [Markdown PDF](https://marketplace.visualstudio.com/items?itemName=yzane.markdown-pdf) honours Mermaid and exports to PDF with vector diagrams. Open any of these four files, run `Cmd/Ctrl+Shift+P → Markdown PDF: Export (pdf)`.

## Source of truth

- **Stack & phases**: [`docs/PROGRESS.md`](../PROGRESS.md) · [`UPGRADE_PLAN.md`](../../UPGRADE_PLAN.md)
- **SQL schema**: [`infra/script/01_init_fuel_schema.sql`](../../infra/script/01_init_fuel_schema.sql) → `09_alter_alerts_multi_pillar.sql`
- **IEA-APERC redesign**: [`infra/script/08_pillars_v2.sql`](../../infra/script/08_pillars_v2.sql)
- **Java source**: `desktop-admin/src/main/java/vn/edu/ves/desktop/` · `backend-api/src/main/java/vn/edu/ves/api/`
