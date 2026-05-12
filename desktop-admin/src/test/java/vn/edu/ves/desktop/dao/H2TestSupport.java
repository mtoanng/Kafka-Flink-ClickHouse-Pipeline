package vn.edu.ves.desktop.dao;

import vn.edu.ves.desktop.util.DatabaseConfig;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper khởi tạo H2 in-memory + inject {@link DatabaseConfig} singleton.
 *
 * <p>Vì {@link DatabaseConfig} là Singleton eager init, ta dùng reflection để
 * override field <code>url/user/password</code> phục vụ test (chấp nhận hack
 * giới hạn trong test scope).</p>
 *
 * <p>Mode H2: <code>jdbc:h2:mem:vesdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1</code></p>
 */
public final class H2TestSupport {

    public static final String H2_URL = "jdbc:h2:mem:vesdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    public static final String H2_USER = "sa";
    public static final String H2_PASSWORD = "";

    private H2TestSupport() {
    }

    /** Override singleton để mọi DAO no-arg đều xài H2. */
    public static void overrideSingletonToH2() {
        DatabaseConfig instance = DatabaseConfig.getInstance();
        try {
            setField(instance, "url", H2_URL);
            setField(instance, "user", H2_USER);
            setField(instance, "password", H2_PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("Không override được DatabaseConfig sang H2", e);
        }
    }

    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
    }

    public static void exec(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    public static void truncateAndDrop(String... objects) throws SQLException {
        try (Connection c = openConnection(); Statement st = c.createStatement()) {
            for (String obj : objects) {
                try {
                    st.execute("DROP VIEW IF EXISTS " + obj);
                } catch (SQLException ignore) {
                    /* fallthrough */
                }
                try {
                    st.execute("DROP TABLE IF EXISTS " + obj);
                } catch (SQLException ignore) {
                    /* table không tồn tại — bỏ qua */
                }
            }
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
