from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from taobao_replay.archive import inspect_jsonl, parse_s3_uri, upload_jsonl


class ArchiveTests(unittest.TestCase):
    def test_inspect_jsonl_is_bounded_and_deterministic(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "events.jsonl"
            path.write_text('{"event_id":"one"}\n{"event_id":"two"}\n', encoding="utf-8")

            stats = inspect_jsonl(path)

            self.assertEqual(2, stats.rows)
            self.assertEqual(path.stat().st_size, stats.bytes)
            self.assertEqual(64, len(stats.sha256))

    def test_upload_records_metadata_without_network(self) -> None:
        class FakeS3:
            def __init__(self) -> None:
                self.call = None

            def upload_file(self, *args, **kwargs):
                self.call = (args, kwargs)

        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "events.jsonl"
            path.write_text(json.dumps({"event_id": "one"}) + "\n", encoding="utf-8")
            client = FakeS3()

            stats = upload_jsonl(path, "s3://demo-bucket/runs/run-1/events.jsonl", client=client)

            self.assertEqual("s3://demo-bucket/runs/run-1/events.jsonl", stats.source)
            self.assertIsNotNone(client.call)
            self.assertEqual("demo-bucket", client.call[0][1])
            self.assertEqual("runs/run-1/events.jsonl", client.call[0][2])
            self.assertEqual(stats.sha256, client.call[1]["ExtraArgs"]["Metadata"]["sha256"])

    def test_s3_uri_requires_bucket_and_key(self) -> None:
        with self.assertRaises(ValueError):
            parse_s3_uri("https://bucket/key")
