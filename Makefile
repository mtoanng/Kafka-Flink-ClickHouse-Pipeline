# =============================================================================
# VES-Monitor — one-command orchestration
# Thay thế Quickstart 7 bước thủ công (bao gồm upload JAR tay qua Flink UI).
#
#   make up        - dựng Docker stack (Kafka, Flink, Postgres, ...)
#   make build     - build toàn bộ Maven modules (skip tests)
#   make submit    - submit Flink job qua REST API (không cần Flink UI)
#   make generate  - chạy 3 data generators nền
#   make demo      - up + build + submit + generate (full demo end-to-end)
#   make down      - dừng generators + toàn bộ stack
#   make health    - smoke check 4/4 services
#
# Windows: chạy qua WSL (wsl -d Ubuntu-22.04 make demo) hoặc Git Bash.
# =============================================================================

SHELL := bash
FLINK_URL ?= http://localhost:8081

.PHONY: up build submit generate demo down health test

up:
	bash scripts/run.sh --wait

build:
	mvn -q clean package -DskipTests

submit:
	FLINK_URL=$(FLINK_URL) bash scripts/submit_flink_job.sh

generate:
	bash scripts/start_generators.sh

demo: up build submit generate
	@echo "✅ Demo stack is up — Flink UI: $(FLINK_URL) | Swagger: http://localhost:8090/swagger-ui.html"

down:
	-bash scripts/stop_generators.sh
	bash scripts/stop.sh

health:
	bash scripts/healthcheck.sh

test:
	mvn -q test
