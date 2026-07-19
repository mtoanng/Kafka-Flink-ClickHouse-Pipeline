#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
from hashlib import sha256
from pathlib import Path

HEADER = ("user_id", "item_id", "category_id", "behavior_type", "timestamp")


def file_sha256(path: Path) -> str:
    digest = sha256()
    with path.open("rb") as handle:
        while block := handle.read(1024 * 1024):
            digest.update(block)
    return digest.hexdigest()


def copy_prefix(source: Path, destination: Path, *, limit: int, force: bool = False) -> int:
    if limit <= 0:
        raise ValueError("limit must be positive")
    if source.resolve() == destination.resolve():
        raise ValueError("input and output paths must differ")
    if destination.exists() and not force:
        raise FileExistsError(f"output exists; pass --force to replace it: {destination}")

    destination.parent.mkdir(parents=True, exist_ok=True)
    copied = 0
    with source.open("r", encoding="utf-8-sig", newline="") as source_handle:
        with destination.open("w", encoding="utf-8", newline="") as destination_handle:
            reader = csv.reader(source_handle)
            writer = csv.writer(destination_handle, lineterminator="\n")
            first_row = True
            for row in reader:
                values = tuple(value.strip() for value in row)
                if first_row and values == HEADER:
                    writer.writerow(row)
                    first_row = False
                    continue
                first_row = False
                writer.writerow(row)
                copied += 1
                if copied == limit:
                    break
    return copied


def main() -> int:
    parser = argparse.ArgumentParser(description="Create a deterministic prefix subset")
    parser.add_argument("input", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--limit", type=int, default=100_000)
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()

    if not args.input.is_file():
        parser.error(f"input file does not exist: {args.input}")
    try:
        copied = copy_prefix(args.input, args.output, limit=args.limit, force=args.force)
    except (OSError, ValueError) as exc:
        parser.error(str(exc))
    print(
        json.dumps(
            {
                "input": str(args.input),
                "output": str(args.output),
                "rows": copied,
                "sha256": file_sha256(args.output),
            },
            sort_keys=True,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
