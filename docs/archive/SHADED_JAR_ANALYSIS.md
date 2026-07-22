# Shaded JAR Analysis

The Phase B package build was inspected rather than silencing Maven Shade
warnings. The ClickHouse `flink-connector-clickhouse-1.17:0.2.0:all` artifact
is intentionally a fat connector and embeds Guava 33.4.6 and HdrHistogram
2.2.2. Kafka and the Cassandra driver previously added duplicate Guava/HdrHistogram
artifacts, including Guava 32.x.

Targeted exclusions now remove the Guava/HdrHistogram transitive copies from
the application dependency graph. The shaded JAR therefore keeps the versions
supplied by the ClickHouse connector. A clean package still reports class
overlaps from independently packaged connector bundles (Flink Avro, Cassandra
protocol/JNR libraries, and the ClickHouse fat artifact), plus `META-INF`
metadata. Those classes were traced to the connector contents rather than
silenced blindly; the current decision is to retain the vendor fat connector
and document the runtime version boundary. A real Flink smoke run remains
required before changing that packaging choice.

This is a dependency-selection decision, not a claim of runtime compatibility.
The Flink cluster must still use a matching connector/runtime distribution and
requires a real deployment smoke test before the JAR is considered verified.
