from __future__ import annotations

import argparse
import json
import sys
from collections.abc import Sequence
from contextlib import ExitStack
from dataclasses import asdict
from pathlib import Path
from typing import IO

from taobao_replay.contracts import UserBehaviorEvent
from taobao_replay.profile import profile_file
from taobao_replay.reader import ParseIssue
from taobao_replay.replay import replay_file
from taobao_replay.source_audit import DatasetContractError, audit_source_dataset


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="taobao-replay")
    subparsers = parser.add_subparsers(dest="command", required=True)

    profile_parser = subparsers.add_parser("profile", help="profile a CSV with bounded memory")
    profile_parser.add_argument("input", type=Path)
    profile_parser.add_argument("--batch-size", type=int, default=10_000)

    audit_parser = subparsers.add_parser(
        "source-audit", help="verify a raw Tianchi source and its acquisition manifest"
    )
    audit_parser.add_argument("input", type=Path)
    audit_parser.add_argument("--manifest", type=Path, required=True)
    audit_parser.add_argument("--batch-size", type=int, default=10_000)

    replay_parser = subparsers.add_parser("replay", help="replay accepted rows to JSONL")
    replay_parser.add_argument("input", type=Path)
    replay_parser.add_argument("--run-id", default="local-run")
    replay_parser.add_argument("--batch-size", type=int, default=1_000)
    replay_parser.add_argument("--speed", type=float, default=0)
    replay_parser.add_argument("--output", default="-", help="JSONL output path or - for stdout")
    replay_parser.add_argument("--invalid-output", help="optional JSONL path for rejected rows")
    replay_parser.add_argument("--force", action="store_true", help="overwrite output files")
    return parser


def _open_output(stack: ExitStack, path: str, *, force: bool) -> IO[str]:
    if path == "-":
        return sys.stdout
    mode = "w" if force else "x"
    return stack.enter_context(Path(path).open(mode, encoding="utf-8", newline="\n"))


def _validate_input(parser: argparse.ArgumentParser, path: Path) -> None:
    if not path.is_file():
        parser.error(f"input file does not exist: {path}")


def _validate_outputs(
    parser: argparse.ArgumentParser,
    input_path: Path,
    output_path: str,
    invalid_output_path: str | None,
    *,
    force: bool,
) -> None:
    requested = [Path(path) for path in (output_path, invalid_output_path) if path and path != "-"]
    resolved = [path.resolve() for path in requested]
    if input_path.resolve() in resolved:
        parser.error("an output path must not overwrite the input file")
    if len(resolved) != len(set(resolved)):
        parser.error("accepted and invalid output paths must differ")
    if not force:
        existing = next((path for path in requested if path.exists()), None)
        if existing is not None:
            parser.error(f"output exists; pass --force to replace it: {existing}")


def main(argv: Sequence[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    _validate_input(parser, args.input)

    try:
        if args.command == "profile":
            print(
                json.dumps(profile_file(args.input, batch_size=args.batch_size).to_dict(), indent=2)
            )
            return 0

        if args.command == "source-audit":
            audit = audit_source_dataset(
                args.input,
                args.manifest,
                batch_size=args.batch_size,
            )
            print(json.dumps(audit.to_dict(), indent=2))
            return 0

        _validate_outputs(
            parser,
            args.input,
            args.output,
            args.invalid_output,
            force=args.force,
        )
        with ExitStack() as stack:
            output = _open_output(stack, args.output, force=args.force)
            invalid_output = (
                _open_output(stack, args.invalid_output, force=args.force)
                if args.invalid_output
                else None
            )

            def emit(event: UserBehaviorEvent) -> None:
                output.write(json.dumps(event.to_dict(), sort_keys=True) + "\n")

            def reject(issue: ParseIssue) -> None:
                if invalid_output is not None:
                    invalid_output.write(json.dumps(issue.to_dict(), sort_keys=True) + "\n")

            stats = replay_file(
                args.input,
                replay_run_id=args.run_id,
                emit=emit,
                on_invalid=reject,
                batch_size=args.batch_size,
                speed=args.speed,
            )
        print(json.dumps(asdict(stats), sort_keys=True), file=sys.stderr)
        return 0
    except (DatasetContractError, OSError, ValueError) as exc:
        parser.error(str(exc))
    return 2
