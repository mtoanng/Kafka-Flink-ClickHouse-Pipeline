from __future__ import annotations

import base64
import json
from collections.abc import Iterable
from dataclasses import dataclass
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlsplit, urlunsplit
from urllib.request import Request, urlopen


class ClickHouseRequestError(RuntimeError):
    """A ClickHouse HTTP request failed before its result could be verified."""


@dataclass(frozen=True)
class ClickHouseHttpClient:
    endpoint: str
    user: str
    password: str
    database: str

    def execute(self, sql: str, *, json_result: bool = False) -> dict[str, object] | None:
        query = sql.rstrip().rstrip(";")
        if json_result:
            query = f"{query}\nFORMAT JSON"
        credentials = base64.b64encode(f"{self.user}:{self.password}".encode()).decode()
        request = Request(
            self._url(),
            data=query.encode(),
            headers={
                "Authorization": f"Basic {credentials}",
                "Content-Type": "text/plain; charset=utf-8",
            },
            method="POST",
        )
        try:
            with urlopen(request, timeout=30) as response:  # noqa: S310 - endpoint is explicit operator input
                body = response.read().decode()
        except HTTPError as exc:
            detail = exc.read().decode(errors="replace").strip()
            raise ClickHouseRequestError(f"ClickHouse returned HTTP {exc.code}: {detail}") from exc
        except URLError as exc:
            raise ClickHouseRequestError(f"ClickHouse request failed: {exc.reason}") from exc
        if not json_result:
            return None
        return json.loads(body)

    def _url(self) -> str:
        parts = urlsplit(self.endpoint)
        if not parts.scheme or not parts.netloc:
            raise ClickHouseRequestError("CLICKHOUSE_ENDPOINT must be an absolute http(s) URL")
        query = parts.query
        separator = "&" if query else ""
        database_query = urlencode({"database": self.database})
        return urlunsplit(
            (parts.scheme, parts.netloc, parts.path, f"{query}{separator}{database_query}", "")
        )


def split_sql_statements(contents: str) -> Iterable[str]:
    uncommented = "\n".join(
        line for line in contents.splitlines() if not line.lstrip().startswith("--")
    )
    for statement in uncommented.split(";"):
        statement = statement.strip()
        if statement:
            yield statement
