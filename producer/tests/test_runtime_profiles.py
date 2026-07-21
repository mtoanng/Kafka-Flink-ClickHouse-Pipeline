import unittest
from pathlib import Path

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]


class RuntimeProfileFilesTests(unittest.TestCase):
    def setUp(self) -> None:
        self.compose = (REPOSITORY_ROOT / "infra/docker-compose.yml").read_text(encoding="utf-8")
        self.environment = (REPOSITORY_ROOT / ".env.example").read_text(encoding="utf-8")

    def test_core_profile_contains_kafka_and_schema_registry(self) -> None:
        self.assertIn("  kafka:", self.compose)
        self.assertIn("  schema-registry:", self.compose)
        self.assertIn('profiles: ["core", "serving", "cdc", "observability"]', self.compose)

    def test_optional_profiles_are_declared(self) -> None:
        self.assertIn('profiles: ["cdc"]', self.compose)
        self.assertIn('profiles: ["observability"]', self.compose)
        self.assertIn("CASSANDRA_ENABLED=false", self.environment)
        self.assertIn("CDC_ENABLED=false", self.environment)
        self.assertIn("OBSERVABILITY_ENABLED=false", self.environment)

    def test_active_environment_has_no_s3_event_archive_variable(self) -> None:
        self.assertNotIn("S3_ARCHIVE_URI", self.environment)


if __name__ == "__main__":
    unittest.main()
