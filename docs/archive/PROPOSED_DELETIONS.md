# Proposed Deletions

This is a proposal only. No file, directory, configuration, state, or cloud
resource was deleted, moved, archived, applied, or destroyed during this audit.

## High-Confidence Cleanup

| Candidate | Proposed action | Reason |
| --- | --- | --- |
| `.env.example`: `S3_ARCHIVE_URI` | Completed | The active environment has no event archive variable. |
| `docs/evidence/phase-5/PHASE_REPORT.md`: S3 archive claims | Retain as immutable historical evidence | It describes a superseded phase and is not active guidance. |
| `docs/evidence/final-e2e/README.md`: S3 archive evidence item | Completed | The active checklist now excludes event archival. |
| `docs/evidence/phase-6/PHASE_REPORT.md`: historical storage-path reference | Retain as immutable historical evidence | Historical phase text is not an active architecture contract. |

No active Python module, Maven dependency, Java class, uploader script,
Terraform resource, or test currently implements an S3 event sink. Terraform
uses local state in the checked-in configuration and defines no S3 backend or
bucket. If an S3 backend or artifact bucket is added later, it is infrastructure
storage and must remain explicitly separate from event data.

## Stale Historical Material

These candidates do not affect builds, but they reduce resume clarity. Decide in
a later cleanup pass whether to remove them or move them outside the active docs
navigation:

| Candidate | Classification | Reason |
| --- | --- | --- |
| `docs/archive/f1-baseline/` | STALE | Superseded project documents contain obsolete domain, schemas, commands and deployment claims. |
| `docs/PHASE_0_MIGRATION_INVENTORY.md` | STALE | Describes modules and dashboards that no longer exist. |
| `docs/evidence/phase-0/` | STALE | Baseline evidence is unrelated to the current Taobao implementation. |
| Legacy references inside phase 1-5 reports | STALE | Historical statements describe old reactor contents and removed components. |
| `docs/CLEANUP_AUDIT.md` | Completed | Updated to describe the active profiles and historical cleanup boundary. |

## Keep

Do not delete the following merely because live deployment is unverified:

- `infra/terraform/` and its provider lock file;
- `.env.cloud.example` and deployment variables after reconciliation;
- Docker Compose and both intended profiles;
- preflight, smoke, evidence, recovery and teardown scripts after correction;
- PostgreSQL, Debezium, compacted-topic and broadcast-state artifacts;
- checkpoint configuration and recovery procedure;
- ClickHouse and Cassandra schemas and verification tools;
- deterministic fixtures and expected reconciliation data.

Generated `target/`, Python caches, Terraform provider cache, raw dataset and
runtime `artifacts/` remain ignored local outputs, not deletion candidates for a
source-control cleanup commit.
