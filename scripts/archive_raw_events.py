from __future__ import annotations

import argparse
import json
from pathlib import Path

from taobao_replay.archive import inspect_jsonl, upload_jsonl


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Archive accepted replay JSONL to S3")
    parser.add_argument("input", type=Path)
    parser.add_argument("--s3-uri", help="s3://bucket/key destination; omit for a local plan")
    parser.add_argument("--manifest", type=Path, help="write the source/upload manifest JSON")
    return parser


def main() -> int:
    args = build_parser().parse_args()
    stats = upload_jsonl(args.input, args.s3_uri) if args.s3_uri else inspect_jsonl(args.input)
    if args.manifest:
        args.manifest.write_text(json.dumps(stats.to_dict(), indent=2) + "\n", encoding="utf-8")
    print(json.dumps(stats.to_dict(), sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
