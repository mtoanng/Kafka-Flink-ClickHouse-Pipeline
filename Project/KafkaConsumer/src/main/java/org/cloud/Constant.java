package org.cloud;

public class Constant {

    public static final String TIMESTAMP_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_STRING_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final class KafkaSourceConfig {
        public static final String TOPIC_NAME         = ConfigLoader.get("kafka.topic");
        public static final String BOOTSTRAP_SERVERS  = ConfigLoader.get("kafka.bootstrap.servers");
        public static final String GROUP_ID           = ConfigLoader.get("kafka.group.id");
        public static final String AUTO_COMMIT        = ConfigLoader.get("enable.auto.commit");
        public static final String OFFSET_RESET       = ConfigLoader.get("auto.offset.reset");
        public static final String SOURCE_NAME        = "Kafka Fuel Price Source";
    }

    public static final class PostgresqlConfig {
        // Tên class driver PostgreSQL (không load từ config vì không bao giờ thay đổi)
        public static final String DRIVER_NAME = "org.postgresql.Driver";

        public static final String JDBC_URL  = ConfigLoader.get("jdbc.url");
        public static final String USERNAME  = ConfigLoader.get("jdbc.username");
        public static final String PASSWORD  = ConfigLoader.get("jdbc.password");
    }

    public static final class WindowConfig {
        /** Window aggregation mỗi 1 phút */
        public static final int TUMBLING_WINDOW_MINUTES = 1;
        /** Cho phép dữ liệu đến trễ tối đa 30 giây */
        public static final int ALLOWED_LATENESS_SECONDS = 30;
    }
}
