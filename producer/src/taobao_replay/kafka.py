from __future__ import annotations

import json
import os
from collections.abc import Mapping
from pathlib import Path
from typing import Any, Protocol

from taobao_replay.contracts import UserBehaviorEvent

DEFAULT_TOPIC = "user-behavior-events"


class ProducerLike(Protocol):
    def produce(self, **kwargs: object) -> None: ...

    def poll(self, timeout: float) -> object: ...

    def flush(self, timeout: float | None = None) -> int: ...


def event_to_avro(event: UserBehaviorEvent, _context: object = None) -> dict[str, str | int]:
    return event.to_dict()


def kafka_key(event: UserBehaviorEvent) -> str:
    return str(event.user_id)


def _optional_environment_config(environment: Mapping[str, str]) -> dict[str, str]:
    mappings = {
        "KAFKA_SECURITY_PROTOCOL": "security.protocol",
        "KAFKA_SASL_MECHANISM": "sasl.mechanism",
        "KAFKA_SASL_USERNAME": "sasl.username",
        "KAFKA_SASL_PASSWORD": "sasl.password",
    }
    return {
        target: environment[source]
        for source, target in mappings.items()
        if environment.get(source, "").strip()
    }


def _load_confluent() -> tuple[type[Any], type[Any], type[Any], type[Any]]:
    try:
        from confluent_kafka import SerializingProducer
        from confluent_kafka.schema_registry import SchemaRegistryClient
        from confluent_kafka.schema_registry.avro import AvroSerializer
        from confluent_kafka.serialization import StringSerializer
    except ImportError as exc:
        raise RuntimeError(
            "Kafka publishing requires the optional dependency: pip install -e '.[kafka]'"
        ) from exc
    return SerializingProducer, SchemaRegistryClient, AvroSerializer, StringSerializer


def build_confluent_producer(
    *,
    bootstrap_servers: str,
    schema_registry_url: str,
    schema_path: Path,
    environment: Mapping[str, str] | None = None,
) -> ProducerLike:
    if not bootstrap_servers.strip():
        raise ValueError("bootstrap_servers must not be blank")
    if not schema_registry_url.strip():
        raise ValueError("schema_registry_url must not be blank")

    schema = json.loads(schema_path.read_text(encoding="utf-8"))
    schema_text = json.dumps(schema, separators=(",", ":"))
    producer_type, registry_type, avro_serializer_type, string_serializer_type = _load_confluent()

    active_environment = os.environ if environment is None else environment
    registry_config: dict[str, str] = {"url": schema_registry_url}
    registry_auth = active_environment.get("SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO", "").strip()
    if registry_auth:
        registry_config["basic.auth.user.info"] = registry_auth

    registry = registry_type(registry_config)
    value_serializer = avro_serializer_type(
        registry,
        schema_str=schema_text,
        to_dict=event_to_avro,
        conf={"auto.register.schemas": False},
    )
    producer_config: dict[str, object] = {
        "bootstrap.servers": bootstrap_servers,
        "key.serializer": string_serializer_type("utf_8"),
        "value.serializer": value_serializer,
        "enable.idempotence": True,
        "acks": "all",
    }
    producer_config.update(_optional_environment_config(active_environment))
    return producer_type(producer_config)


class KafkaEventPublisher:
    def __init__(self, producer: ProducerLike, *, topic: str = DEFAULT_TOPIC) -> None:
        if not topic.strip():
            raise ValueError("topic must not be blank")
        self._producer = producer
        self._topic = topic
        self._delivery_errors: list[str] = []

    def publish(self, event: UserBehaviorEvent) -> None:
        def delivered(error: object, _message: object) -> None:
            if error is not None:
                self._delivery_errors.append(str(error))

        self._producer.produce(
            topic=self._topic,
            key=kafka_key(event),
            value=event,
            on_delivery=delivered,
        )
        self._producer.poll(0)

    def close(self, timeout: float = 30.0) -> None:
        remaining = self._producer.flush(timeout)
        if remaining:
            raise RuntimeError(f"Kafka flush timed out with {remaining} message(s) pending")
        if self._delivery_errors:
            raise RuntimeError(f"Kafka delivery failed: {self._delivery_errors[0]}")
