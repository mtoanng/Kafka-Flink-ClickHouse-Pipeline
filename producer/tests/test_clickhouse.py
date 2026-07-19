from __future__ import annotations

import unittest

from taobao_replay.clickhouse import (
    ClickHouseHttpClient,
    ClickHouseRequestError,
    split_sql_statements,
)


class ClickHouseScriptContractTest(unittest.TestCase):
    def test_schema_splitter_ignores_comments_and_keeps_two_statements(self) -> None:
        statements = list(
            split_sql_statements(
                "-- phase comment\nCREATE DATABASE demo;\n\n"
                "-- table comment\nCREATE TABLE demo.x (id UInt64);"
            )
        )

        self.assertEqual(["CREATE DATABASE demo", "CREATE TABLE demo.x (id UInt64)"], statements)

    def test_client_preserves_existing_endpoint_parameters_and_adds_database(self) -> None:
        client = ClickHouseHttpClient(
            "https://example.invalid:8443/?compress=1", "default", "secret", "taobao_behavior"
        )

        self.assertEqual(
            "https://example.invalid:8443/?compress=1&database=taobao_behavior", client._url()
        )

    def test_client_rejects_relative_endpoint_without_a_network_request(self) -> None:
        client = ClickHouseHttpClient("localhost:8123", "default", "", "taobao_behavior")

        with self.assertRaises(ClickHouseRequestError):
            client._url()
