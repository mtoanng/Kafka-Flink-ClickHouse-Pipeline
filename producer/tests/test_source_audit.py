from __future__ import annotations

import json
import tempfile
import unittest
from hashlib import sha256
from pathlib import Path

from taobao_replay.source_audit import DatasetContractError, audit_source_dataset

OFFICIAL_URL = "https://tianchi.aliyun.com/dataset/dataDetail?dataId=649"
RAW_ROWS = (
    "1,10,100,pv,1511658000\n"
    "2,11,101,cart,1511658001\n"
    "3,12,102,fav,1511658002\n"
    "4,13,103,buy,1511658003\n"
)


class SourceAuditTests(unittest.TestCase):
    def _candidate(
        self,
        directory: str,
        *,
        content: str = RAW_ROWS,
        source_url: str = OFFICIAL_URL,
        file_name: str = "UserBehavior.csv",
        expected_hash: str | None = None,
        expected_rows: int = 4,
    ) -> tuple[Path, Path]:
        source = Path(directory) / file_name
        source.write_text(content, encoding="utf-8")
        manifest = Path(directory) / "source-manifest.json"
        manifest.write_text(
            json.dumps(
                {
                    "dataset_name": "User Behavior Data from Taobao for Recommendation",
                    "dataset_kind": "raw_event_rows",
                    "source_url": source_url,
                    "file_name": file_name,
                    "acquired_at_utc": "2026-07-19T12:00:00Z",
                    "expected_sha256": expected_hash or sha256(source.read_bytes()).hexdigest(),
                    "expected_row_count": expected_rows,
                }
            ),
            encoding="utf-8",
        )
        return source, manifest

    def test_official_raw_five_column_source_satisfies_contract(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(directory)
            audit = audit_source_dataset(source, manifest, batch_size=2)

        self.assertEqual(audit.status, "SATISFIED")
        self.assertEqual(audit.profile.source_rows, 4)
        self.assertEqual(audit.profile.invalid_rows, 0)
        self.assertEqual(
            audit.profile.behavior_counts,
            {"buy": 1, "cart": 1, "fav": 1, "pv": 1},
        )

    def test_non_tianchi_source_is_rejected_even_when_shape_matches(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(
                directory,
                source_url="https://www.kaggle.com/datasets/example/taobao-cleaned",
            )
            with self.assertRaisesRegex(DatasetContractError, "official Tianchi"):
                audit_source_dataset(source, manifest)

    def test_derived_six_column_input_is_rejected(self) -> None:
        content = "1,10,100,pv,1511658000,derived-feature\n"
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(directory, content=content, expected_rows=1)
            with self.assertRaisesRegex(DatasetContractError, "outside the five-column contract"):
                audit_source_dataset(source, manifest)

    def test_semantically_invalid_raw_row_is_preserved_for_flink_rejection(self) -> None:
        content = RAW_ROWS + "0,14,104,pv,1511658004\n"
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(directory, content=content, expected_rows=5)
            audit = audit_source_dataset(source, manifest)

        self.assertEqual(audit.status, "SATISFIED")
        self.assertEqual(audit.profile.accepted_rows, 5)
        self.assertEqual(audit.profile.invalid_rows, 0)

    def test_headered_input_is_rejected(self) -> None:
        header = "user_id,item_id,category_id,behavior_type,timestamp\n"
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(directory, content=header + RAW_ROWS)
            with self.assertRaisesRegex(DatasetContractError, "headerless"):
                audit_source_dataset(source, manifest)

    def test_hash_mismatch_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(directory, expected_hash="0" * 64)
            with self.assertRaisesRegex(DatasetContractError, "SHA-256"):
                audit_source_dataset(source, manifest)

    def test_wrong_filename_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            source, manifest = self._candidate(directory, file_name="cleaned.csv")
            with self.assertRaisesRegex(DatasetContractError, "source filename"):
                audit_source_dataset(source, manifest)


if __name__ == "__main__":
    unittest.main()
