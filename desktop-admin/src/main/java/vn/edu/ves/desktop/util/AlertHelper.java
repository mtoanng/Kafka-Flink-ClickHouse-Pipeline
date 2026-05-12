package vn.edu.ves.desktop.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Static helper cho JavaFX {@link Alert} (thường dùng trong controller).
 *
 * <p>Tách ra thành class riêng để:</p>
 * <ul>
 *   <li>Controller không phải import javafx.scene.control.* mỗi nơi.</li>
 *   <li>Thống nhất title/text style (tránh mỗi nơi gõ một kiểu).</li>
 *   <li>Dễ swap sang custom dialog ở phase sau mà không sửa controller.</li>
 * </ul>
 */
public final class AlertHelper {

    private AlertHelper() {
    }

    public static void showError(String title, String message) {
        show(AlertType.ERROR, title, message);
    }

    public static void showWarning(String title, String message) {
        show(AlertType.WARNING, title, message);
    }

    public static void showInfo(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    /**
     * Hiển thị confirm dialog kiểu Yes/No, trả về TRUE nếu user click OK.
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
