from __future__ import annotations

from collections import Counter
from dataclasses import asdict, dataclass
from hashlib import sha256
from pathlib import Path

from taobao_replay.reader import iter_event_batches


@dataclass(frozen=True, slots=True)
class DatasetProfile:
    path: str
    byte_size: int
    sha256: str
    source_rows: int
    accepted_rows: int
    invalid_rows: int
    invalid_reason_counts: dict[str, int]
    min_event_time_ms: int | None
    max_event_time_ms: int | None
    behavior_counts: dict[str, int]
    backward_time_jumps: int

    def to_dict(self) -> dict[str, object]:
        return asdict(self)


def sha256_file(path: Path, block_size: int = 1024 * 1024) -> str:
    digest = sha256()
    with path.open("rb") as handle:
        while block := handle.read(block_size):
            digest.update(block)
    return digest.hexdigest()


def profile_file(path: Path, *, batch_size: int = 10_000) -> DatasetProfile:
    counts: Counter[str] = Counter()
    source_rows = 0
    accepted_rows = 0
    invalid_rows = 0
    invalid_reasons: Counter[str] = Counter()
    minimum: int | None = None
    maximum: int | None = None
    previous: int | None = None
    backward_time_jumps = 0

    for batch in iter_event_batches(path, replay_run_id="profile", batch_size=batch_size):
        source_rows += batch.source_rows
        invalid_rows += len(batch.issues)
        invalid_reasons.update(issue.reason for issue in batch.issues)
        for event in batch.events:
            accepted_rows += 1
            counts[event.behavior_type] += 1
            minimum = event.event_time_ms if minimum is None else min(minimum, event.event_time_ms)
            maximum = event.event_time_ms if maximum is None else max(maximum, event.event_time_ms)
            if previous is not None and event.event_time_ms < previous:
                backward_time_jumps += 1
            previous = event.event_time_ms

    return DatasetProfile(
        path=str(path),
        byte_size=path.stat().st_size,
        sha256=sha256_file(path),
        source_rows=source_rows,
        accepted_rows=accepted_rows,
        invalid_rows=invalid_rows,
        invalid_reason_counts=dict(sorted(invalid_reasons.items())),
        min_event_time_ms=minimum,
        max_event_time_ms=maximum,
        behavior_counts=dict(sorted(counts.items())),
        backward_time_jumps=backward_time_jumps,
    )
