from __future__ import annotations

import argparse
import json
import os

from taobao_replay.clickhouse import ClickHouseHttpClient

FIXTURE_WINDOW_START = "2017-11-26 01:00:00"


def parser() -> argparse.ArgumentParser:
    result = argparse.ArgumentParser(description="Verify ClickHouse fixture assertions")
    result.add_argument("--run-id", required=True)
    result.add_argument("--expected-raw-count", type=int, default=999)
    result.add_argument("--endpoint", default=os.getenv("CLICKHOUSE_ENDPOINT"))
    result.add_argument("--user", default=os.getenv("CLICKHOUSE_USER", "default"))
    result.add_argument("--password", default=os.getenv("CLICKHOUSE_PASSWORD", ""))
    result.add_argument("--database", default=os.getenv("CLICKHOUSE_DATABASE", "taobao_behavior"))
    return result


def sql_literal(value: str) -> str:
    return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"


def query_rows(client: ClickHouseHttpClient, sql: str) -> list[dict[str, object]]:
    result = client.execute(sql, json_result=True)
    assert result is not None
    return list(result["data"])


def main() -> int:
    args = parser().parse_args()
    if not args.endpoint:
        parser().error("set CLICKHOUSE_ENDPOINT or pass --endpoint")
    client = ClickHouseHttpClient(args.endpoint, args.user, args.password, args.database)
    run_id = sql_literal(args.run_id)

    raw_rows = query_rows(
        client,
        f"SELECT count() AS raw_count FROM raw_behavior_events WHERE replay_run_id = {run_id}",
    )
    raw_count = int(raw_rows[0]["raw_count"])
    if raw_count != args.expected_raw_count:
        raise SystemExit(f"raw count mismatch: expected {args.expected_raw_count}, got {raw_count}")

    metrics_rows = query_rows(
        client,
        "SELECT pv_count, cart_count, fav_count, buy_count, unique_users "
        "FROM item_metrics_1m "
        f"WHERE replay_run_id = {run_id} AND item_id = 500 "
        f"AND window_start = toDateTime64('{FIXTURE_WINDOW_START}', 3, 'UTC')",
    )
    expected_metrics = {
        "pv_count": 0,
        "cart_count": 1,
        "fav_count": 0,
        "buy_count": 1,
        "unique_users": 1,
    }
    if len(metrics_rows) != 1:
        raise SystemExit(f"expected one item 500 fixture metric row, got {len(metrics_rows)}")
    actual_metrics = {key: int(value) for key, value in metrics_rows[0].items()}
    if actual_metrics != expected_metrics:
        raise SystemExit(
            "item 500 metric mismatch: "
            f"expected {json.dumps(expected_metrics, sort_keys=True)}, "
            f"got {json.dumps(actual_metrics, sort_keys=True)}"
        )

    print(json.dumps({"raw_count": raw_count, "item_500_metrics": actual_metrics}, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
