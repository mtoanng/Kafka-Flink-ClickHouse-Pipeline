# Checkpoint / Restart Experiment

Status: `NOT VERIFIED` until a disposable Flink host produces real checkpoint and restart logs.

## Procedure

1. Set `FLINK_CHECKPOINTING_ENABLED=true` and `FLINK_CHECKPOINT_DIR` to durable remote storage.
2. Run `bash scripts/run_checkpoint_experiment.sh` to print the active policy and procedure.
3. Submit the detached job with `FLINK_DETACHED=true bash scripts/run_flink.sh`.
4. Record a completed checkpoint and the job ID from the Flink CLI/UI.
5. Cause one controlled TaskManager/job failure on the disposable host.
6. Confirm the job resumes from the checkpoint, then compare accepted/raw/current-state counts with the bounded fixture.
7. Save the real CLI/UI evidence here and run `bash scripts/teardown_demo.sh` after the demo.

The job uses `AT_LEAST_ONCE` checkpoint mode. The ClickHouse and Scylla sinks have not been proven exactly once, so this experiment must not be described as exactly-once recovery.
