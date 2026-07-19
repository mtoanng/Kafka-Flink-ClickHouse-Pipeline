from __future__ import annotations

import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = REPOSITORY_ROOT / "producer/src"


class CliTests(unittest.TestCase):
    def test_replay_refuses_to_overwrite_its_input(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "source.csv"
            original = b"1,2,3,pv,1511658000\n"
            source.write_bytes(original)
            environment = os.environ.copy()
            environment["PYTHONPATH"] = str(SOURCE_ROOT)
            result = subprocess.run(
                [
                    sys.executable,
                    "-m",
                    "taobao_replay",
                    "replay",
                    str(source),
                    "--output",
                    str(source),
                    "--force",
                ],
                check=False,
                capture_output=True,
                text=True,
                env=environment,
            )

            self.assertEqual(result.returncode, 2)
            self.assertIn("must not overwrite the input", result.stderr)
            self.assertEqual(source.read_bytes(), original)


if __name__ == "__main__":
    unittest.main()
