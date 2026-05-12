package vn.edu.ves.desktop.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper mỏng quanh <code>org.mindrot.jbcrypt.BCrypt</code> để dễ unit-test
 * và để tách dependency BCrypt khỏi service layer.
 *
 * <p>Seed users từ Phase 2 (admin/manager/user) dùng hash <code>$2b$10$...</code>
 * (sinh bằng Python <code>bcrypt</code>). jBCrypt 0.4 chỉ chấp nhận prefix
 * <code>$2a$</code>/<code>$2y$</code>, nên ta normalize <code>$2b$</code> →
 * <code>$2a$</code> trước khi verify. Hai version dùng cùng thuật toán cho
 * password ≤ 72 ký tự (chỉ khác sign-extension fix cho password &gt; 72 char,
 * không relevant ở use case này).</p>
 */
public final class PasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int DEFAULT_LOG_ROUNDS = 10;

    private PasswordUtil() {
    }

    /**
     * Verify plaintext password với BCrypt hash. Trả về <code>false</code> nếu
     * input null/blank hoặc hash sai format (defensive — không ném exception).
     */
    public static boolean verify(String plain, String hash) {
        if (plain == null || plain.isEmpty() || hash == null || hash.isEmpty()) {
            return false;
        }
        String normalized = normalizeBcryptPrefix(hash);
        try {
            return BCrypt.checkpw(plain, normalized);
        } catch (IllegalArgumentException ex) {
            log.warn("BCrypt hash sai format ({}): {}", hash, ex.getMessage());
            return false;
        }
    }

    /**
     * jBCrypt 0.4 không hỗ trợ prefix <code>$2b$</code> (sinh bởi Python bcrypt
     * hoặc Spring Security 5+). Convert sang <code>$2a$</code> để verify được.
     */
    private static String normalizeBcryptPrefix(String hash) {
        if (hash.startsWith("$2b$")) {
            return "$2a$" + hash.substring(4);
        }
        return hash;
    }

    /**
     * Sinh BCrypt hash mới từ plaintext, dùng log rounds = 10 (đồng bộ seed Phase 2).
     */
    public static String hash(String plain) {
        if (plain == null) {
            throw new IllegalArgumentException("Password không được null");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(DEFAULT_LOG_ROUNDS));
    }
}
