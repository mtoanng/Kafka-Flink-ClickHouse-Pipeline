package vn.edu.ves.desktop.widget;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Small slide-in toast for CRITICAL alerts.
 *
 * <p>Implementation: {@link Popup} anchored to the top-right of the owner
 * {@link Window}, auto-dismissed after {@link #DEFAULT_DISMISS_MS} ms with
 * a 250 ms fade-out.</p>
 */
public final class Toast {

    public static final long DEFAULT_DISMISS_MS = 5000L;

    private Toast() {
    }

    /** Show a CRITICAL-styled toast and auto-dismiss after the default timeout. */
    public static Popup showCritical(Window owner, String title, String message) {
        return show(owner, title, message, "toast-critical", DEFAULT_DISMISS_MS);
    }

    /** Show with custom CSS class and dismissal interval. */
    public static Popup show(Window owner, String title, String message,
                              String styleClass, long dismissMs) {
        if (owner == null) return null;

        Label titleLbl = new Label(title == null ? "Alert" : title);
        titleLbl.getStyleClass().add("toast-title");
        Label msgLbl = new Label(message == null ? "" : message);
        msgLbl.getStyleClass().add("toast-message");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(360);

        VBox box = new VBox(4, titleLbl, msgLbl);
        box.getStyleClass().add("toast");
        if (styleClass != null) box.getStyleClass().add(styleClass);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + colorFor(styleClass) + ";" +
                "-fx-border-width: 0 0 0 4;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0.2, 0, 4);");

        Popup popup = new Popup();
        popup.getContent().add(box);
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        double x = owner.getX() + owner.getWidth() - 400;
        double y = owner.getY() + 80;
        popup.show(owner, x, y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), box);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        PauseTransition hold = new PauseTransition(Duration.millis(Math.max(500, dismissMs)));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), box);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        SequentialTransition seq = new SequentialTransition(fadeIn, hold, fadeOut);
        seq.setOnFinished(ev -> popup.hide());
        seq.play();
        return popup;
    }

    private static String colorFor(String styleClass) {
        if (styleClass == null) return "#1976D2";
        switch (styleClass) {
            case "toast-critical": return "#C62828";
            case "toast-warning":  return "#EF6C00";
            case "toast-success":  return "#2E7D32";
            default:               return "#1976D2";
        }
    }
}
