#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
from pathlib import Path

DEFAULT_OUTPUT = Path(__file__).resolve().parents[1] / "tests/fixtures/user_behavior_fixture.csv"
TOTAL_ROWS = 1_000


def fixture_rows() -> list[tuple[object, object, object, str, object]]:
    rows: list[tuple[object, object, object, str, object]] = [
        (100, 500, 50, "cart", 1_511_658_000),
        (102, 502, 50, "pv", 1_511_658_010),
        (100, 500, 50, "buy", 1_511_658_020),
        (101, 501, 51, "cart", 1_511_658_021),
        (103, 503, 52, "fav", 1_511_658_022),
        (104, 504, 52, "pv", 1_511_658_030),
        (105, 505, 53, "pv", 1_511_658_015),
        (103, 503, 52, "fav", 1_511_658_022),
        ("invalid-user", 506, 53, "pv", 1_511_658_031),
    ]
    behaviors = ("pv", "cart", "fav", "buy")
    for index in range(TOTAL_ROWS - len(rows)):
        rows.append(
            (
                1_000 + (index % 200),
                2_000 + index,
                300 + (index % 25),
                behaviors[index % len(behaviors)],
                1_511_658_100 + index,
            )
        )
    return rows


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate the deterministic Phase 1 fixture")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()

    if args.output.exists() and not args.force:
        parser.error(f"output exists; pass --force to replace it: {args.output}")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle, lineterminator="\n")
        writer.writerows(fixture_rows())
    print(f"wrote {TOTAL_ROWS} rows to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
