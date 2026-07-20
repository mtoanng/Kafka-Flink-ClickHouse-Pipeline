#!/usr/bin/env python3
"""Build credential-free expected counts for the bounded release fixture."""

from __future__ import annotations

import json
import os
from collections import Counter
from pathlib import Path

from taobao_replay.reader import iter_event_batches


def main() -> int:
    fixture = Path(os.environ.get("FIXTURE_PATH", "tests/fixtures/user_behavior_fixture.csv"))
    run_id = os.environ.get("REPLAY_RUN_ID", "release-reconciliation")
    accepted = []
    rejected = 0
    source_rows = 0
    for batch in iter_event_batches(fixture, replay_run_id=run_id):
        accepted.extend(batch.events)
        rejected += len(batch.issues)
        source_rows += batch.source_rows
    behaviors = Counter(str(event.behavior_type) for event in accepted)
    item_500 = Counter(str(event.behavior_type) for event in accepted if event.item_id == 500)
    result = {
        "fixture": str(fixture),
        "source_rows": source_rows,
        "accepted_rows": len(accepted),
        "rejected_rows": rejected,
        "behavior_counts": dict(sorted(behaviors.items())),
        "item_500_behavior_counts": dict(sorted(item_500.items())),
        "unique_users": len({event.user_id for event in accepted}),
    }
    evidence_dir = Path(os.environ.get("EVIDENCE_DIR", "docs/evidence/final-e2e"))
    evidence_dir.mkdir(parents=True, exist_ok=True)
    output = evidence_dir / "expected-reconciliation.json"
    output.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(json.dumps(result, sort_keys=True))
    print(f"wrote {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
