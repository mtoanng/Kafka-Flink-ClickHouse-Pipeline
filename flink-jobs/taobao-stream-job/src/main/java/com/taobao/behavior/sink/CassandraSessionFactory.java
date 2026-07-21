package com.taobao.behavior.sink;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;

public class CassandraSessionFactory {
    public CqlSession open(CassandraConfig config) {
        return CqlSession.builder()
                .withCloudSecureConnectBundle(config.secureBundlePath())
                .withAuthCredentials("token", config.applicationToken())
                .withKeyspace(CqlIdentifier.fromCql(config.keyspace()))
                .build();
    }
}
