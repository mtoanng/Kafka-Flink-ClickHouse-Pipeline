package vn.edu.ves.desktop.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator pattern <em>Strategy</em>. Mỗi strategy là 1 lambda <code>String → Optional&lt;errorMsg&gt;</code>.
 *
 * <p>Trong Phase 5.3 ta cần verify form Region (code, name, country, vn_zone), Phase 5.4 reuse cho
 * form AlertRule + User. Thay vì viết if-else trong từng controller, ta compose Strategy:</p>
 *
 * <pre>
 *   Validator&lt;String&gt; codeValidator = Validator.compose(
 *       new NotBlankValidator("Mã region"),
 *       new PatternValidator("Mã region", "^[A-Z0-9_]{2,20}$",
 *           "chỉ chứa A-Z, 0-9 và _, dài 2-20 ký tự"),
 *       new LengthRangeValidator("Mã region", 2, 20));
 *
 *   List&lt;String&gt; errors = codeValidator.validate("VN_NORTH");  // empty list = OK
 * </pre>
 *
 * <p>Đây là Strategy + Composite hợp nhất: từng Validator là Strategy đơn lẻ, compose() trả về
 * ValidatorChain gộp nhiều Strategy.</p>
 */
public interface Validator<T> {

    /** Trả về danh sách lỗi (empty nghĩa là PASS). */
    List<String> validate(T value);

    /* ================== Builtin strategies ================== */

    /** Compose nhiều Validator thành 1 chain. Chạy hết tất cả, gom toàn bộ lỗi. */
    @SafeVarargs
    static <T> Validator<T> compose(Validator<T>... validators) {
        return value -> {
            List<String> all = new ArrayList<>();
            for (Validator<T> v : validators) {
                all.addAll(v.validate(value));
            }
            return all;
        };
    }

    /** Strategy: không được rỗng / null / blank. */
    class NotBlankValidator implements Validator<String> {
        private final String fieldName;
        public NotBlankValidator(String fieldName) { this.fieldName = fieldName; }
        @Override
        public List<String> validate(String value) {
            if (value == null || value.trim().isEmpty()) {
                return Collections.singletonList(fieldName + " không được để trống");
            }
            return Collections.emptyList();
        }
    }

    /** Strategy: chiều dài trong khoảng [min, max] (inclusive). Null/blank → bỏ qua (để NotBlank lo). */
    class LengthRangeValidator implements Validator<String> {
        private final String fieldName;
        private final int min;
        private final int max;
        public LengthRangeValidator(String fieldName, int min, int max) {
            this.fieldName = fieldName;
            this.min = min;
            this.max = max;
        }
        @Override
        public List<String> validate(String value) {
            if (value == null || value.trim().isEmpty()) return Collections.emptyList();
            int len = value.trim().length();
            if (len < min || len > max) {
                return Collections.singletonList(
                        fieldName + " phải có độ dài từ " + min + " đến " + max + " ký tự (hiện: " + len + ")");
            }
            return Collections.emptyList();
        }
    }

    /** Strategy: match regex pattern. Null/blank → bỏ qua. */
    class PatternValidator implements Validator<String> {
        private final String fieldName;
        private final Pattern pattern;
        private final String requirement;
        public PatternValidator(String fieldName, String regex, String requirement) {
            this.fieldName = fieldName;
            this.pattern = Pattern.compile(regex);
            this.requirement = requirement;
        }
        @Override
        public List<String> validate(String value) {
            if (value == null || value.trim().isEmpty()) return Collections.emptyList();
            if (!pattern.matcher(value.trim()).matches()) {
                return Collections.singletonList(fieldName + " " + requirement);
            }
            return Collections.emptyList();
        }
    }

    /** Strategy: value phải nằm trong set cho phép. Null/blank → bỏ qua. */
    class InSetValidator implements Validator<String> {
        private final String fieldName;
        private final List<String> allowed;
        public InSetValidator(String fieldName, String... allowed) {
            this.fieldName = fieldName;
            this.allowed = Arrays.asList(allowed);
        }
        @Override
        public List<String> validate(String value) {
            if (value == null || value.trim().isEmpty()) return Collections.emptyList();
            if (!allowed.contains(value.trim())) {
                return Collections.singletonList(
                        fieldName + " phải là một trong " + allowed + " (hiện: " + value + ")");
            }
            return Collections.emptyList();
        }
    }
}
