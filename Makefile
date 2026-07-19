SHELL := bash

.PHONY: test package infra-config remote-up remote-down topic schema publish-fixture run-job

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

publish-fixture:
	bash scripts/replay.sh

run-job:
	bash scripts/run_flink.sh
