from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
FIXTURE = REPOSITORY_ROOT / "tests/fixtures/user_behavior_fixture.csv"
SCRIPT = REPOSITORY_ROOT / "scripts/prepare_demo_subset.py"


class PrepareDemoSubsetTests(unittest.TestCase):
    def test_script_copies_a_deterministic_prefix(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            first = Path(directory) / "first.csv"
            second = Path(directory) / "second.csv"
            for output in (first, second):
                result = subprocess.run(
                    [sys.executable, str(SCRIPT), str(FIXTURE), str(output), "--limit", "25"],
                    check=False,
                    capture_output=True,
                    text=True,
                )
                self.assertEqual(result.returncode, 0, result.stderr)
            self.assertEqual(first.read_bytes(), second.read_bytes())
            self.assertEqual(len(first.read_text(encoding="utf-8").splitlines()), 25)


if __name__ == "__main__":
    unittest.main()
