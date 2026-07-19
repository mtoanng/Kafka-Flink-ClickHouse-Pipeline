from __future__ import annotations

import unittest
from pathlib import Path

from taobao_replay.profile import profile_file

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
FIXTURE = REPOSITORY_ROOT / "tests/fixtures/user_behavior_fixture.csv"


class ProfileTests(unittest.TestCase):
    def test_fixture_profile(self) -> None:
        profile = profile_file(FIXTURE, batch_size=31)
        self.assertEqual(profile.source_rows, 1_000)
        self.assertEqual(profile.accepted_rows, 999)
        self.assertEqual(profile.invalid_rows, 1)
        self.assertEqual(
            profile.behavior_counts,
            {"buy": 248, "cart": 250, "fav": 250, "pv": 251},
        )
        self.assertEqual(profile.backward_time_jumps, 1)
        self.assertEqual(profile.min_event_time_ms, 1_511_658_000_000)
        self.assertEqual(profile.max_event_time_ms, 1_511_659_090_000)


if __name__ == "__main__":
    unittest.main()
