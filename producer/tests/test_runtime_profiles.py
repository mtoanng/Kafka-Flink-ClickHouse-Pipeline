import unittest
from pathlib import Path

REPOSITORY_ROOT = Path(__file__).resolve().parents[2]


class RuntimeProfileFilesTests(unittest.TestCase):
    def setUp(self) -> None:
        self.compose = (REPOSITORY_ROOT / "infra/docker-compose.yml").read_text(encoding="utf-8")
        self.environment = (REPOSITORY_ROOT / ".env.example").read_text(encoding="utf-8")

    def test_core_profile_contains_every_data_plane_service(self) -> None:
        for service in ("kafka", "schema-registry", "clickhouse", "cassandra"):
            self.assertIn(f"  {service}:", self.compose)
        self.assertIn('profiles: ["core", "full"]', self.compose)

    def test_full_profile_adds_control_plane_and_grafana(self) -> None:
        for service in ("postgres", "debezium-connect", "grafana"):
            self.assertIn(f"  {service}:", self.compose)
        self.assertIn('profiles: ["full"]', self.compose)

    def test_environment_makes_cassandra_and_checkpointing_core_contracts(self) -> None:
        self.assertIn("RUNTIME_PROFILE=core", self.environment)
        self.assertIn("RUNTIME_DEPENDENCIES=local", self.environment)
        self.assertIn("CASSANDRA_MODE=local", self.environment)
        self.assertIn("CASSANDRA_HOSTS=localhost", self.environment)
        self.assertIn("FLINK_CHECKPOINTING_ENABLED=true", self.environment)
        self.assertNotIn("CASSANDRA_ENABLED=", self.environment)

    def test_active_environment_has_no_s3_event_archive_variable(self) -> None:
        self.assertNotIn("S3_ARCHIVE_URI", self.environment)


if __name__ == "__main__":
    unittest.main()
