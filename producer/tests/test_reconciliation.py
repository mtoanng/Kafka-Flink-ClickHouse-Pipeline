from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
SCRIPT = REPOSITORY_ROOT / "scripts/reconcile_final_e2e.py"
FIXTURE = REPOSITORY_ROOT / "tests/fixtures/user_behavior_fixture.csv"


def load_reconciliation_module():
    spec = importlib.util.spec_from_file_location("reconcile_final_e2e", SCRIPT)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class ReconciliationTests(unittest.TestCase):
    def test_fixture_expected_results_cover_every_serving_branch(self) -> None:
        result = load_reconciliation_module().build_expected(FIXTURE, "test-run")

        self.assertEqual(1_000, result["source_rows"])
        self.assertEqual(1_000, result["produced_events"])
        self.assertEqual(999, result["accepted_raw_events"])
        self.assertEqual(1, result["invalid_events"])
        self.assertEqual(2, result["late_events"])
        self.assertEqual(997, result["on_time_events"])
        self.assertEqual({"buy": 1, "cart": 1}, result["item_500_behavior_counts"])
        self.assertEqual(1, result["item_500_unique_users"])
        self.assertEqual([501], result["user_100_active_cart_item_ids"])


if __name__ == "__main__":
    unittest.main()
