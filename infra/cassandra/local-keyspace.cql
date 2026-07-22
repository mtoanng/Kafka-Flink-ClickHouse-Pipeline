-- Local single-node development only. Managed Cassandra/Astra keyspaces are
-- provisioned separately and must not use SimpleStrategy.
CREATE KEYSPACE IF NOT EXISTS taobao_streaming
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
