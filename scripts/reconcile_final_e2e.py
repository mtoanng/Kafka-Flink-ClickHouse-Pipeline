#!/usr/bin/env python3
"""Compute bounded expected results independently of the Java Flink job."""

from __future__ import annotations

import json
import os
from collections import Counter, defaultdict
from pathlib import Path

from taobao_replay.contracts import UserBehaviorEvent
from taobao_replay.reader import iter_event_batches

MAX_OUT_OF_ORDERNESS_MS = 5_000


def semantic_invalid_reason(event: UserBehaviorEvent) -> str | None:
    if min(event.user_id, event.item_id, event.category_id) <= 0:
        return "IDs must be positive"
    if event.event_time_ms < 0:
        return "event_time_ms must be non-negative"
    return None


def build_expected(fixture: Path, run_id: str) -> dict[str, object]:
    produced: list[UserBehaviorEvent] = []
    producer_rejected = 0
    source_rows = 0
    for batch in iter_event_batches(fixture, replay_run_id=run_id):
        produced.extend(batch.events)
        producer_rejected += len(batch.issues)
        source_rows += batch.source_rows

    valid: list[UserBehaviorEvent] = []
    invalid: list[UserBehaviorEvent] = []
    for event in produced:
        (invalid if semantic_invalid_reason(event) else valid).append(event)

    maximum_timestamp: int | None = None
    on_time: list[UserBehaviorEvent] = []
    late: list[UserBehaviorEvent] = []
    for event in valid:
        maximum_timestamp = (
            event.event_time_ms
            if maximum_timestamp is None
            else max(maximum_timestamp, event.event_time_ms)
        )
        watermark = maximum_timestamp - MAX_OUT_OF_ORDERNESS_MS - 1
        (late if event.event_time_ms <= watermark else on_time).append(event)

    active_cart: dict[int, dict[int, UserBehaviorEvent]] = defaultdict(dict)
    latest_order: dict[tuple[int, int], tuple[int, int]] = {}
    for event in on_time:
        if event.behavior_type not in {"cart", "buy"}:
            continue
        key = (event.user_id, event.item_id)
        order = (event.event_time_ms, event.source_sequence)
        if order <= latest_order.get(key, (-1, -1)):
            continue
        latest_order[key] = order
        if event.behavior_type == "cart":
            active_cart[event.user_id][event.item_id] = event
        else:
            active_cart[event.user_id].pop(event.item_id, None)

    item_500 = Counter(event.behavior_type for event in on_time if event.item_id == 500)
    valid_behaviors = Counter(event.behavior_type for event in valid)
    return {
        "fixture": str(fixture),
        "source_rows": source_rows,
        "produced_events": len(produced),
        "producer_rejected_rows": producer_rejected,
        "accepted_raw_events": len(valid),
        "invalid_events": len(invalid),
        "late_events": len(late),
        "on_time_events": len(on_time),
        "valid_behavior_counts": dict(sorted(valid_behaviors.items())),
        "item_500_behavior_counts": dict(sorted(item_500.items())),
        "item_500_unique_users": len({event.user_id for event in on_time if event.item_id == 500}),
        "user_100_active_cart_item_ids": sorted(active_cart[100]),
    }


def main() -> int:
    fixture = Path(os.environ.get("FIXTURE_PATH", "tests/fixtures/user_behavior_fixture.csv"))
    run_id = os.environ.get("REPLAY_RUN_ID", "release-reconciliation")
    result = build_expected(fixture, run_id)
    evidence_dir = Path(os.environ.get("EVIDENCE_DIR", "docs/evidence/final-e2e"))
    evidence_dir.mkdir(parents=True, exist_ok=True)
    output = evidence_dir / "expected-reconciliation.json"
    output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps(result, sort_keys=True))
    print(f"wrote {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
