package vn.edu.ves.desktop.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.exception.AuthenticationException;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.service.AuthService;
import vn.edu.ves.desktop.service.AuthServiceImpl;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Controller cho <code>fxml/login.fxml</code>.
 *
 * <p>Trách nhiệm:</p>
 * <ul>
 *   <li>Wire field <code>username/password</code> → gọi {@link AuthService#login(String, String)}.</li>
 *   <li>Disable nút login khi đang xác thực để tránh double-submit.</li>
 *   <li>Load màn Dashboard sau khi login OK (Phase 5.2 sẽ thay placeholder bằng dashboard thật).</li>
 * </ul>
 */
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private static final String DASHBOARD_FXML = "/fxml/dashboard.fxml";
    private static final String DASHBOARD_PLACEHOLDER_FXML = "/fxml/dashboard_placeholder.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private AuthService authService = new AuthServiceImpl();

    /** Setter cho test (inject mock AuthService). */
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        if (lblError != null) {
            lblError.setText("");
        }
        if (txtUsername != null && txtPassword != null) {
            txtPassword.setOnAction(this::handleLogin);
            txtUsername.setOnAction(e -> txtPassword.requestFocus());
        }
        Platform.runLater(() -> {
            if (txtUsername != null) {
                txtUsername.requestFocus();
            }
        });
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        clearError();
        String username = txtUsername == null ? "" : txtUsername.getText();
        String password = txtPassword == null ? "" : txtPassword.getText();
        setBusy(true);
        try {
            User user = authService.login(username, password);
            log.info("Login OK — user={}, role={}", user.getUsername(), user.getRole());
            switchToDashboard();
        } catch (AuthenticationException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            log.error("Lỗi không mong đợi khi login", ex);
            showError("Lỗi hệ thống, vui lòng thử lại sau");
        } finally {
            setBusy(false);
        }
    }

    private void switchToDashboard() {
        try {
            URL fxmlUrl = getClass().getResource(DASHBOARD_FXML);
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource(DASHBOARD_PLACEHOLDER_FXML);
            }
            Objects.requireNonNull(fxmlUrl, "Không tìm thấy dashboard FXML trên classpath");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            URL css = getClass().getResource(MATERIAL_CSS);
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(1024);
            stage.setMinHeight(700);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Không load được dashboard sau khi login", e);
            showError("Không mở được màn hình Dashboard: " + e.getMessage());
        }
    }

    private void setBusy(boolean busy) {
        if (btnLogin != null) {
            btnLogin.setDisable(busy);
            btnLogin.setText(busy ? "Đang đăng nhập..." : "Đăng nhập");
        }
        if (txtUsername != null) {
            txtUsername.setDisable(busy);
        }
        if (txtPassword != null) {
            txtPassword.setDisable(busy);
        }
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }

    private void clearError() {
        if (lblError != null) {
            lblError.setText("");
        }
    }
}
