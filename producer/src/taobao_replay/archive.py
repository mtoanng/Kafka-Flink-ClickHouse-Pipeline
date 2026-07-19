from __future__ import annotations

import hashlib
import json
from dataclasses import asdict, dataclass
from pathlib import Path
from urllib.parse import urlparse


@dataclass(frozen=True)
class ArchiveStats:
    source: str
    rows: int
    bytes: int
    sha256: str

    def to_dict(self) -> dict[str, object]:
        return asdict(self)


def inspect_jsonl(path: Path) -> ArchiveStats:
    digest = hashlib.sha256()
    rows = 0
    size = 0
    with path.open("rb") as source:
        for line in source:
            digest.update(line)
            size += len(line)
            if line.strip():
                json.loads(line)
                rows += 1
    return ArchiveStats(str(path), rows, size, digest.hexdigest())


def parse_s3_uri(uri: str) -> tuple[str, str]:
    parsed = urlparse(uri)
    if parsed.scheme != "s3" or not parsed.netloc or not parsed.path.strip("/"):
        raise ValueError("archive destination must be an s3://bucket/key URI")
    return parsed.netloc, parsed.path.lstrip("/")


def upload_jsonl(path: Path, uri: str, *, client: object | None = None) -> ArchiveStats:
    stats = inspect_jsonl(path)
    bucket, key = parse_s3_uri(uri)
    if client is None:
        try:
            import boto3
        except ImportError as exc:
            raise RuntimeError(
                "install the archive extra to upload to S3: pip install -e '.[archive]'"
            ) from exc
        client = boto3.client("s3")
    client.upload_file(
        str(path),
        bucket,
        key,
        ExtraArgs={"ContentType": "application/x-ndjson", "Metadata": {"sha256": stats.sha256}},
    )
    return ArchiveStats(uri, stats.rows, stats.bytes, stats.sha256)
