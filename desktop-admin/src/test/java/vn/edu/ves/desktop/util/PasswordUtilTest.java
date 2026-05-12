package vn.edu.ves.desktop.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4 tests cho {@link PasswordUtil}.
 *
 * <p>Sử dụng hash thực của seed user "admin" từ 04_seed_basic.sql để khẳng định
 * jBCrypt verify được hash <code>$2b$10$...</code> (sinh bởi Python bcrypt).</p>
 */
public class PasswordUtilTest {

    /** Hash thực của password "admin" (copy từ 04_seed_basic.sql). */
    private static final String ADMIN_HASH = "$2b$10$Oja1u17iYTrz6O/NqzeOtOZrBpXqxDCVgJ4Agcu/Tt.a4Exo2d2PS";

    @Test
    public void verify_correctPassword_returnsTrue() {
        assertTrue("Hash $2b$ của 'admin' phải match plaintext 'admin'",
                PasswordUtil.verify("admin", ADMIN_HASH));
    }

    @Test
    public void verify_wrongPassword_returnsFalse() {
        assertFalse(PasswordUtil.verify("wrongPassword", ADMIN_HASH));
    }

    @Test
    public void verify_nullOrBlank_returnsFalse() {
        assertFalse(PasswordUtil.verify(null, ADMIN_HASH));
        assertFalse(PasswordUtil.verify("", ADMIN_HASH));
        assertFalse(PasswordUtil.verify("admin", null));
        assertFalse(PasswordUtil.verify("admin", ""));
    }

    @Test
    public void verify_malformedHash_returnsFalse() {
        assertFalse("Hash format sai phải return false thay vì throw",
                PasswordUtil.verify("admin", "not-a-bcrypt-hash"));
    }

    @Test
    public void hash_producesValidVerifiableHash() {
        String hash = PasswordUtil.hash("mySecret123");
        assertNotNull(hash);
        assertNotEquals("mySecret123", hash);
        assertTrue("Hash mới sinh phải verify được",
                PasswordUtil.verify("mySecret123", hash));
        assertFalse(PasswordUtil.verify("anotherSecret", hash));
    }
}
