from __future__ import annotations

import argparse
import os
from pathlib import Path

from taobao_replay.clickhouse import ClickHouseHttpClient, split_sql_statements


def parser() -> argparse.ArgumentParser:
    result = argparse.ArgumentParser(description="Apply the Phase 3 ClickHouse DDL over HTTPS")
    result.add_argument("--endpoint", default=os.getenv("CLICKHOUSE_ENDPOINT"))
    result.add_argument("--user", default=os.getenv("CLICKHOUSE_USER", "default"))
    result.add_argument("--password", default=os.getenv("CLICKHOUSE_PASSWORD", ""))
    result.add_argument("--database", default=os.getenv("CLICKHOUSE_DATABASE", "taobao_behavior"))
    result.add_argument("--ddl", type=Path, default=Path("infra/clickhouse/schema.sql"))
    return result


def main() -> int:
    args = parser().parse_args()
    if not args.endpoint:
        parser().error("set CLICKHOUSE_ENDPOINT or pass --endpoint")
    statements = list(split_sql_statements(args.ddl.read_text(encoding="utf-8")))
    client = ClickHouseHttpClient(args.endpoint, args.user, args.password, args.database)
    for statement in statements:
        client.execute(statement)
    print(f"Applied {len(statements)} ClickHouse DDL statements to {args.database}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
