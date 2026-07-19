SHELL := bash

.PHONY: test package infra-config remote-up remote-down topic schema scylla-schema publish-fixture run-job lookup-user archive-fixture checkpoint-experiment terraform-validate teardown

test:
	PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
	ruff check producer scripts
	ruff format --check producer scripts
	mvn -B test

package:
	mvn -B -pl flink-jobs/taobao-stream-job -am package

infra-config:
	docker compose -f infra/docker-compose.yml config --quiet

remote-up:
	bash scripts/run.sh

remote-down:
	bash scripts/stop.sh

topic:
	bash scripts/create_topic.sh

schema:
	bash scripts/register_schemas.sh

scylla-schema:
	bash scripts/apply_scylla_schema.sh

publish-fixture:
	bash scripts/replay.sh

run-job:
	bash scripts/run_flink.sh

lookup-user:
	bash scripts/lookup_current_activity.sh $(USER_ID)

archive-fixture:
	PYTHONPATH=producer/src python scripts/archive_raw_events.py artifacts/events.jsonl --manifest artifacts/events.archive.json

checkpoint-experiment:
	bash scripts/run_checkpoint_experiment.sh

terraform-validate:
	terraform -chdir=infra/terraform fmt -check
	terraform -chdir=infra/terraform init -backend=false -input=false
	terraform -chdir=infra/terraform validate

teardown:
	bash scripts/teardown_demo.sh
