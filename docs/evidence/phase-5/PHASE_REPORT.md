# Phase 5 Report

## 1. Phase Implemented

Implemented Phase 5 only: bounded S3 raw archiving, opt-in Flink checkpoint configuration and restart procedure, CI coverage for Python/Maven/Terraform, minimal Terraform for the S3 archive bucket, and explicit teardown guards. No Phase 6 CDC/control-plane work was started.

The archive path is intentionally operator-controlled: a local run computes a JSONL manifest, while an S3 upload occurs only when an explicit `s3://bucket/key` destination is supplied. Checkpointing is disabled by default on the laptop and requires a durable `FLINK_CHECKPOINT_DIR`.

## 2. Files Changed

- Added `producer/src/taobao_replay/archive.py`, the optional `archive` dependency, archive tests, and `scripts/archive_raw_events.py`.
- Added `CheckpointPolicy` and tests; the Java job now supports opt-in `AT_LEAST_ONCE` checkpoint storage.
- Added `docs/evidence/phase-5/CHECKPOINT_EXPERIMENT.md` and `scripts/run_checkpoint_experiment.sh`.
- Added `infra/terraform/` S3 bucket/versioning/encryption/lifecycle configuration, committed provider lock, and guarded `scripts/teardown_demo.sh`.
- Updated `.gitlab-ci.yml`, `.gitignore`, `.env.example`, `Makefile`, `README.md`, the Phase 4 fixture workflow, and this report.

## 3. Commands and Results

| Command | Exit | Result |
| --- | ---: | --- |
| `ruff check producer scripts` | 0 | All checks passed. |
| `ruff format --check producer scripts` | 0 | 27 files already formatted. |
| `PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v` | 0 | 32 tests passed, including local S3 archive contracts. |
| `python -m compileall -q producer/src producer/tests scripts` | 0 | Python sources compiled. |
| `terraform -chdir=infra/terraform fmt -check` | 0 | Terraform files formatted. |
| `terraform -chdir=infra/terraform init -backend=false -input=false` | 0 | AWS provider `5.100.0` selected; no backend or resource was created. |
| `terraform -chdir=infra/terraform validate` | 0 | Configuration valid. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am test` | 0 | 17 Taobao tests passed, including checkpoint policy tests. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am package -DskipTests` | 0 | Phase 5 shaded JAR built successfully. |
| `mvn -B test` | 0 | Four-module reactor passed; Taobao ran 17 tests. |
| Git Bash `bash -n` over Phase 5 scripts | 0 | Archive, checkpoint, teardown, and existing workflow scripts parsed. |
| Fixture archive dry-run | 0 | 999 JSONL rows, 250,616 bytes, SHA-256 recorded in ignored `artifacts/`. |
| `git diff --check` | 0 | No whitespace errors. |
| `bash scripts/run_checkpoint_experiment.sh` without `FLINK_CHECKPOINT_DIR` | 1 (expected) | Refused to prepare a remote checkpoint run. |
| `bash scripts/teardown_demo.sh` without confirmation | 2 (expected) | Refused to destroy Terraform resources. |

## 4. Verified Acceptance Criteria

- Archive inspection validates every JSONL line, counts rows/bytes, and computes a deterministic SHA-256 manifest.
- S3 upload uses an explicit `s3://` URI and optional boto3 extra; local tests use a fake client and make no network request.
- Terraform defines only an encrypted, versioned, lifecycle-managed S3 bucket with a required bucket name and no backend.
- Checkpoint configuration is opt-in, validates boolean/interval/path inputs, and uses `AT_LEAST_ONCE` mode with durable storage set by environment.
- CI now covers the Taobao/F1 Maven reactor, Python tests/lint/format, and Terraform fmt/init/validate.
- Teardown requires `CONFIRM_TEARDOWN=YES` and explicit Terraform variables; it was not executed.

## 5. NOT VERIFIED

- `NOT VERIFIED`: S3 credentials, IAM permissions, object upload from a real bounded demo, archive retrieval, lifecycle expiration, and cloud teardown.
- `NOT VERIFIED`: Flink checkpoint creation, TaskManager/job failure, restart-from-checkpoint, state consistency, and real logs. See [CHECKPOINT_EXPERIMENT.md](CHECKPOINT_EXPERIMENT.md).
- `NOT VERIFIED`: CI execution on GitLab runners, Docker image availability, network provider download in CI, and security scan results.
- `NOT VERIFIED`: remote Kafka/Flink/ClickHouse/Scylla integration, connector retry behavior, performance, cost, and exactly-once claims.

## 6. Blockers and Risks

- Cloud credentials and an approved disposable environment are required for S3 and checkpoint evidence; none were provisioned.
- Flink S3 checkpoint paths may require the matching Flink filesystem plugin on the remote cluster; this was not tested locally.
- The archive uploader is at-least-once operational tooling and does not deduplicate objects beyond its caller-selected key.
- Terraform `force_destroy` defaults to false; deleting a non-empty bucket requires an explicit, reviewed override.
- Shaded dependency overlap warnings remain in the existing Flink job build and need remote runtime evidence.

## 7. Student Learning Task

Explain what a completed Flink checkpoint can restore and what it cannot guarantee when the ClickHouse, ScyllaDB, and S3 sinks are not proven exactly once.

## 8. Teach-Back Questions and Controlled Failure

1. Why is checkpointing disabled by default on the laptop, and what makes `FLINK_CHECKPOINT_DIR` durable?
2. Why does the archive manifest include both byte count and SHA-256 instead of only row count?
3. Why does Terraform require an explicit bucket name and guarded teardown?

Controlled failure: run `bash scripts/run_checkpoint_experiment.sh` without `FLINK_CHECKPOINT_DIR`. Predicted and observed result: exit 1 before Flink CLI or cloud access. A second guard, `bash scripts/teardown_demo.sh`, exits 2 without `CONFIRM_TEARDOWN=YES`.

## 9. Cleanup Report

Removed:
- The obsolete F1-only GitLab CI job names and F1-only environment example contents were replaced with Taobao Phase 5 checks/configuration.

Archived:
- None.

Kept:
- Terraform state and provider cache remain ignored; the provider lock file is kept for reproducibility.
- F1 replay/job/Grafana baseline remains frozen and outside the Taobao Phase 5 path.
- Existing ClickHouse and Scylla contracts remain because Phase 5 adds archive/recovery/operations around them.

Reason:
- Phase 5 extends the current data plane without adding a second streaming job or Phase 6 control plane. Sensitive local `.env` and raw data remain unmodified and untracked.

## 10. Phase Boundary Confirmation

No Phase 6 work was started. No PostgreSQL, Debezium, broadcast state, configuration topic, cart timers, or alert-rule control plane was added.
