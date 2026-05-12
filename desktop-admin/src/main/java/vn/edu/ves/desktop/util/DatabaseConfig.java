package vn.edu.ves.desktop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton config loader + thin {@link Connection} factory.
 *
 * <p>Resolution order cho mỗi key:</p>
 * <ol>
 *   <li>System property (vd <code>-Ddb.url=...</code>)</li>
 *   <li>Environment variable dạng UPPER_SNAKE (vd <code>DB_URL</code>)</li>
 *   <li>Giá trị trong <code>application.properties</code> trên classpath</li>
 * </ol>
 *
 * <p>Phase 5.0: <strong>Singleton qua eager init</strong> — pattern Singleton checklist.
 * Phase 5.5: sẽ thêm test với @TempProperties override.</p>
 */
public final class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = "application.properties";
    private static final DatabaseConfig INSTANCE = new DatabaseConfig();

    private final String url;
    private final String user;
    private final String password;
    private final int queryTimeoutSec;

    private DatabaseConfig() {
        Properties props = loadProperties();
        this.url = resolve(props, "db.url", "jdbc:postgresql://localhost:5432/fuel_prices");
        this.user = resolve(props, "db.user", "postgres");
        this.password = resolve(props, "db.password", "123456");
        this.queryTimeoutSec = Integer.parseInt(resolve(props, "db.query.timeout.s", "10"));
        log.info("DatabaseConfig loaded — url={}, user={}, queryTimeout={}s",
                this.url, this.user, this.queryTimeoutSec);
    }

    public static DatabaseConfig getInstance() {
        return INSTANCE;
    }

    /** Mở connection mới — caller tự đóng (try-with-resources). */
    public Connection openConnection() throws SQLException {
        Connection c = DriverManager.getConnection(url, user, password);
        c.setAutoCommit(true);
        return c;
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public int getQueryTimeoutSec() { return queryTimeoutSec; }

    /* --------------------- internal --------------------- */

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                log.warn("{} không có trên classpath — dùng defaults", CONFIG_FILE);
            } else {
                props.load(in);
                log.debug("Loaded {} keys từ {}", props.size(), CONFIG_FILE);
            }
        } catch (IOException e) {
            log.error("Lỗi đọc {}: {}", CONFIG_FILE, e.getMessage());
        }
        return props;
    }

    private static String resolve(Properties props, String key, String fallback) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();

        String envKey = key.toUpperCase().replace('.', '_');
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env.trim();

        String prop = props.getProperty(key);
        return (prop != null && !prop.isBlank()) ? prop.trim() : fallback;
    }
}
