from __future__ import annotations

import csv
import subprocess
import sys
import tempfile
import tracemalloc
import unittest
from pathlib import Path

from taobao_replay.reader import iter_event_batches

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
FIXTURE = REPOSITORY_ROOT / "tests/fixtures/user_behavior_fixture.csv"
GENERATOR = REPOSITORY_ROOT / "scripts/generate_fixture.py"


class ReaderTests(unittest.TestCase):
    def test_fixture_is_bounded_and_routes_invalid_row(self) -> None:
        batches = list(iter_event_batches(FIXTURE, replay_run_id="test", batch_size=17))
        self.assertEqual(sum(batch.source_rows for batch in batches), 1_000)
        self.assertEqual(sum(len(batch.events) for batch in batches), 999)
        self.assertEqual(sum(len(batch.issues) for batch in batches), 1)
        self.assertLessEqual(max(batch.source_rows for batch in batches), 17)

    def test_reader_preserves_out_of_order_source_event(self) -> None:
        events = [
            event
            for batch in iter_event_batches(FIXTURE, replay_run_id="test", batch_size=10)
            for event in batch.events
        ]
        by_sequence = {event.source_sequence: event for event in events}
        self.assertLess(by_sequence[6].event_time_ms, by_sequence[5].event_time_ms)

    def test_optional_header_does_not_change_first_source_sequence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "header.csv"
            path.write_text(
                "user_id,item_id,category_id,behavior_type,timestamp\n1,2,3,pv,10\n",
                encoding="utf-8",
            )
            batch = next(iter_event_batches(path, replay_run_id="test", batch_size=10))
            self.assertEqual(batch.events[0].source_sequence, 0)

    def test_fixture_has_required_duplicate_and_cart_scenarios(self) -> None:
        with FIXTURE.open(encoding="utf-8", newline="") as handle:
            rows = list(csv.reader(handle))
        self.assertEqual(rows[4], rows[7])
        self.assertIn(["100", "500", "50", "cart", "1511658000"], rows)
        self.assertIn(["100", "501", "51", "cart", "1511658010"], rows)
        self.assertIn(["100", "500", "50", "buy", "1511658020"], rows)
        active_items: set[str] = set()
        for user_id, item_id, _category_id, behavior, _timestamp in rows:
            if user_id != "100":
                continue
            if behavior == "cart":
                active_items.add(item_id)
            elif behavior == "buy":
                active_items.discard(item_id)
        self.assertEqual({"501"}, active_items)
        self.assertIn(["101", "501", "51", "cart", "1511658021"], rows)
        self.assertFalse(any(row[:4] == ["101", "501", "51", "buy"] for row in rows))

    def test_fixture_matches_generator_byte_for_byte(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            generated = Path(directory) / "fixture.csv"
            result = subprocess.run(
                [sys.executable, str(GENERATOR), "--output", str(generated)],
                check=False,
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertEqual(generated.read_bytes(), FIXTURE.read_bytes())

    def test_large_generated_input_keeps_batch_memory_bounded(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "large.csv"
            with path.open("w", encoding="utf-8", newline="") as handle:
                for sequence in range(50_000):
                    handle.write(f"{sequence + 1},{sequence + 2},3,pv,{1511658000 + sequence}\n")

            tracemalloc.start()
            rows = 0
            largest_batch = 0
            for batch in iter_event_batches(path, replay_run_id="memory-test", batch_size=127):
                rows += batch.source_rows
                largest_batch = max(largest_batch, batch.source_rows)
            _, peak_bytes = tracemalloc.get_traced_memory()
            tracemalloc.stop()

        self.assertEqual(rows, 50_000)
        self.assertEqual(largest_batch, 127)
        self.assertLess(peak_bytes, 8 * 1024 * 1024)


if __name__ == "__main__":
    unittest.main()
