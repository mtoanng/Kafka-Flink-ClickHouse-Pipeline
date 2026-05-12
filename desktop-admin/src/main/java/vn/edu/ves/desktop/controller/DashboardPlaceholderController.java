package vn.edu.ves.desktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.service.AuthService;
import vn.edu.ves.desktop.service.AuthServiceImpl;
import vn.edu.ves.desktop.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Controller tạm cho <code>fxml/dashboard_placeholder.fxml</code>.
 *
 * <p>Mục đích Phase 5.1: chứng minh flow login → load FXML mới → hiển thị user info → logout.
 * Phase 5.2 sẽ thay bằng DashboardController với 4 pillar TabPane.</p>
 */
public class DashboardPlaceholderController {

    private static final Logger log = LoggerFactory.getLogger(DashboardPlaceholderController.class);
    private static final String LOGIN_FXML = "/fxml/login.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";

    @FXML private Label lblWelcome;
    @FXML private Label lblRole;
    @FXML private Button btnLogout;

    private final AuthService authService = new AuthServiceImpl();

    @FXML
    public void initialize() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u != null) {
            String fullName = u.getFullName() == null || u.getFullName().isBlank()
                    ? u.getUsername() : u.getFullName();
            lblWelcome.setText("Xin chào, " + fullName);
            lblRole.setText("Vai trò: " + u.getRole());
        } else {
            lblWelcome.setText("(Chưa đăng nhập)");
            lblRole.setText("");
        }
    }

    @FXML
    public void handleLogout() {
        authService.logout();
        try {
            URL fxmlUrl = Objects.requireNonNull(getClass().getResource(LOGIN_FXML),
                    "Không tìm thấy " + LOGIN_FXML + " trên classpath");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(root, 480, 360);
            URL css = getClass().getResource(MATERIAL_CSS);
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(480);
            stage.setMinHeight(360);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Không load lại login screen", e);
        }
    }
}
