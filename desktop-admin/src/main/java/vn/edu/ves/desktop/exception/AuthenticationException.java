package vn.edu.ves.desktop.exception;

/**
 * Ném từ {@link vn.edu.ves.desktop.service.AuthService#login(String, String)}
 * khi user/password sai, account bị disable, hoặc DB error.
 *
 * <p>Runtime exception để Controller không phải declare checked throws khắp nơi —
 * LoginController bắt block riêng trong handleLogin().</p>
 */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
