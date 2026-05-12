package vn.edu.ves.desktop.model;

/**
 * So sánh operator cho AlertRule (constraint chk_alert_rules_operator: &gt;, &lt;, &gt;=, &lt;=, =).
 *
 * <p>Method {@link #fromSymbol(String)} parse từ ký hiệu DB,
 * {@link #toSymbol()} render ngược lại để bind PreparedStatement.</p>
 */
public enum Operator {
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("=");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String toSymbol() {
        return symbol;
    }

    public static Operator fromSymbol(String s) {
        if (s == null) return EQ;
        switch (s.trim()) {
            case ">":  return GT;
            case ">=": return GTE;
            case "<":  return LT;
            case "<=": return LTE;
            case "=":  return EQ;
            default:
                throw new IllegalArgumentException("Operator không hợp lệ: " + s);
        }
    }

    @Override
    public String toString() {
        return symbol;
    }
}
