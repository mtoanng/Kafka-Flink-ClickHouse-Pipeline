package vn.edu.ves.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Lỗi nghiệp vụ — controller throw, {@link GlobalExceptionHandler} dịch sang JSON 4xx.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code   = code;
    }

    public static ApiException unauthorized(String msg)  { return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", msg); }
    public static ApiException forbidden(String msg)     { return new ApiException(HttpStatus.FORBIDDEN,    "FORBIDDEN",    msg); }
    public static ApiException notFound(String msg)      { return new ApiException(HttpStatus.NOT_FOUND,    "NOT_FOUND",    msg); }
    public static ApiException badRequest(String msg)    { return new ApiException(HttpStatus.BAD_REQUEST,  "BAD_REQUEST",  msg); }
    public static ApiException conflict(String msg)      { return new ApiException(HttpStatus.CONFLICT,     "CONFLICT",     msg); }

    public HttpStatus getStatus() { return status; }
    public String     getCode()   { return code;   }
}
