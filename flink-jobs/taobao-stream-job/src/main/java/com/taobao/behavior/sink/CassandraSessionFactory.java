package com.taobao.behavior.sink;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import java.net.InetSocketAddress;

public class CassandraSessionFactory {
    public CqlSession open(CassandraConfig config) {
        DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
                .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, config.connectTimeout())
                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, config.requestTimeout())
                .build();
        CqlSessionBuilder builder = CqlSession.builder()
                .withConfigLoader(loader)
                .withKeyspace(CqlIdentifier.fromCql(config.keyspace()));
        if ("astra".equals(config.mode())) {
            return builder
                    .withCloudSecureConnectBundle(config.secureBundlePath())
                    .withAuthCredentials("token", config.applicationToken())
                    .build();
        }
        for (String host : config.hosts()) {
            builder.addContactPoint(new InetSocketAddress(host, config.port()));
        }
        builder.withLocalDatacenter(config.datacenter());
        if (config.username() != null) {
            builder.withAuthCredentials(config.username(), config.password());
        }
        return builder.build();
    }
}
