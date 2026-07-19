from __future__ import annotations

import csv
import json
import re
from dataclasses import asdict, dataclass
from datetime import datetime
from pathlib import Path
from urllib.parse import parse_qs, urlparse

from taobao_replay.contracts import FIELD_NAMES
from taobao_replay.profile import DatasetProfile, profile_file

OFFICIAL_HOST = "tianchi.aliyun.com"
OFFICIAL_DATA_ID = "649"
EXPECTED_FILE_NAME = "UserBehavior.csv"
RAW_DATASET_KIND = "raw_event_rows"
SHA256_PATTERN = re.compile(r"^[0-9a-f]{64}$")


class DatasetContractError(ValueError):
    """Raised when a candidate source cannot satisfy the raw Tianchi contract."""


@dataclass(frozen=True, slots=True)
class SourceManifest:
    dataset_name: str
    dataset_kind: str
    source_url: str
    file_name: str
    acquired_at_utc: str
    expected_sha256: str
    expected_row_count: int


@dataclass(frozen=True, slots=True)
class SourceAudit:
    status: str
    manifest: SourceManifest
    profile: DatasetProfile

    def to_dict(self) -> dict[str, object]:
        return asdict(self)


def _is_official_source_url(source_url: str) -> bool:
    parsed = urlparse(source_url)
    if parsed.scheme != "https" or parsed.hostname != OFFICIAL_HOST:
        return False
    query_data_id = parse_qs(parsed.query).get("dataId", [])
    normalized_path = parsed.path.rstrip("/")
    return query_data_id == [OFFICIAL_DATA_ID] or normalized_path.endswith(
        f"/dataset/{OFFICIAL_DATA_ID}"
    )


def _parse_acquired_at(value: str) -> None:
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError as exc:
        raise DatasetContractError("acquired_at_utc must be an ISO-8601 timestamp") from exc
    if parsed.tzinfo is None:
        raise DatasetContractError("acquired_at_utc must include a timezone")


def load_manifest(path: Path) -> SourceManifest:
    try:
        payload = json.loads(path.read_text(encoding="utf-8"))
        manifest = SourceManifest(**payload)
    except (OSError, json.JSONDecodeError, TypeError) as exc:
        raise DatasetContractError(f"cannot load source manifest {path}: {exc}") from exc

    if manifest.dataset_kind != RAW_DATASET_KIND:
        raise DatasetContractError(f"dataset_kind must be {RAW_DATASET_KIND!r}")
    if not _is_official_source_url(manifest.source_url):
        raise DatasetContractError(
            "source_url must be the official Tianchi dataset dataId=649 over HTTPS"
        )
    if manifest.file_name != EXPECTED_FILE_NAME:
        raise DatasetContractError(f"manifest file_name must be {EXPECTED_FILE_NAME!r}")
    normalized_hash = manifest.expected_sha256.lower()
    if not SHA256_PATTERN.fullmatch(normalized_hash):
        raise DatasetContractError("expected_sha256 must contain exactly 64 hexadecimal characters")
    if manifest.expected_row_count <= 0:
        raise DatasetContractError("expected_row_count must be positive")
    if not manifest.dataset_name.strip():
        raise DatasetContractError("dataset_name must not be blank")
    _parse_acquired_at(manifest.acquired_at_utc)
    return SourceManifest(
        dataset_name=manifest.dataset_name,
        dataset_kind=manifest.dataset_kind,
        source_url=manifest.source_url,
        file_name=manifest.file_name,
        acquired_at_utc=manifest.acquired_at_utc,
        expected_sha256=normalized_hash,
        expected_row_count=manifest.expected_row_count,
    )


def _reject_header(path: Path) -> None:
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        first_row = next(csv.reader(handle), None)
    if first_row is not None and tuple(value.strip() for value in first_row) == FIELD_NAMES:
        raise DatasetContractError("raw Tianchi UserBehavior.csv must be headerless")


def audit_source_dataset(
    source_path: Path, manifest_path: Path, *, batch_size: int = 10_000
) -> SourceAudit:
    if source_path.name != EXPECTED_FILE_NAME:
        raise DatasetContractError(f"source filename must be {EXPECTED_FILE_NAME!r}")
    if not source_path.is_file():
        raise DatasetContractError(f"source file does not exist: {source_path}")
    if batch_size <= 0:
        raise DatasetContractError("batch_size must be positive")

    manifest = load_manifest(manifest_path)
    if manifest.file_name != source_path.name:
        raise DatasetContractError("manifest file_name does not match the source path")
    _reject_header(source_path)

    profile = profile_file(source_path, batch_size=batch_size)
    if profile.source_rows == 0:
        raise DatasetContractError("source file is empty")
    invalid_shape_rows = sum(
        count
        for reason, count in profile.invalid_reason_counts.items()
        if reason.startswith("expected 5 columns, got ")
    )
    if invalid_shape_rows:
        raise DatasetContractError(
            f"raw source contains {invalid_shape_rows} rows outside the five-column contract"
        )
    if profile.sha256 != manifest.expected_sha256:
        raise DatasetContractError("source SHA-256 does not match the acquisition manifest")
    if profile.source_rows != manifest.expected_row_count:
        raise DatasetContractError(
            "source row count does not match the acquisition manifest: "
            f"expected {manifest.expected_row_count}, got {profile.source_rows}"
        )
    return SourceAudit(status="SATISFIED", manifest=manifest, profile=profile)
