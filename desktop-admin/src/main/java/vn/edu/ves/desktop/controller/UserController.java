package vn.edu.ves.desktop.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.Role;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.service.UserService;
import vn.edu.ves.desktop.service.UserServiceImpl;
import vn.edu.ves.desktop.util.AlertHelper;
import vn.edu.ves.desktop.util.SessionManager;
import vn.edu.ves.desktop.util.Validator;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * CRUD User — admin-only màn hình.
 *
 * <p>Onload: kiểm tra {@link SessionManager#isAdmin()}. Nếu không phải admin,
 * hiển thị warning + tự back về dashboard.</p>
 */
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String DASHBOARD_FXML = "/fxml/dashboard.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";

    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private ChoiceBox<Role> cbRole;
    @FXML private CheckBox chkEnabled;
    @FXML private PasswordField txtNewPassword;
    @FXML private Label lblMode;
    @FXML private Label lblError;

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, Role> colRole;
    @FXML private TableColumn<User, Boolean> colEnabled;
    @FXML private TableColumn<User, String> colLastLogin;

    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;

    private final ObservableList<User> data = FXCollections.observableArrayList();
    private UserService service = new UserServiceImpl();
    private User editing = new User();

    private final Validator<String> usernameValidator = Validator.compose(
            new Validator.NotBlankValidator("Username"),
            new Validator.LengthRangeValidator("Username", 3, 50),
            new Validator.PatternValidator("Username", "^[a-zA-Z0-9_.-]+$",
                    "chỉ chấp nhận chữ, số, _ . -"));
    private final Validator<String> emailValidator = new Validator.PatternValidator(
            "Email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            "không đúng định dạng email");
    private final Validator<String> passwordValidator = new Validator.LengthRangeValidator(
            "Mật khẩu", 4, 100);

    public void setService(UserService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        configureTable();
        cbRole.setItems(FXCollections.observableArrayList(Role.values()));
        cbRole.getSelectionModel().select(Role.VIEWER);
        if (!SessionManager.getInstance().isAdmin()) {
            Platform.runLater(this::denyAccessAndBack);
            return;
        }
        clearForm();
        reload();
        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) populateForm(newV);
        });
    }

    private void denyAccessAndBack() {
        AlertHelper.showWarning("Không có quyền",
                "Chỉ ADMIN mới có thể quản lý user. Tự động quay về Dashboard.");
        handleBackToDashboard();
    }

    private void configureTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colEnabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        colLastLogin.setCellValueFactory(cell -> {
            var ts = cell.getValue() == null ? null : cell.getValue().getLastLoginAt();
            return new javafx.beans.property.SimpleStringProperty(ts == null ? "(chưa)" : ts.toString());
        });
        tblUsers.setItems(data);
    }

    @FXML
    public void reload() {
        clearError();
        try {
            List<User> all = service.findAll();
            data.setAll(all);
        } catch (Exception e) {
            log.error("Reload users fail", e);
            showError("Không tải được danh sách user: " + e.getMessage());
        }
    }

    @FXML
    public void handleNew() {
        editing = new User();
        clearForm();
        if (lblMode != null) lblMode.setText("Thêm mới");
    }

    @FXML
    public void handleSave() {
        clearError();
        StringBuilder errors = new StringBuilder();
        appendErrors(errors, usernameValidator.validate(txtUsername.getText()));
        String email = txtEmail.getText();
        if (email != null && !email.isBlank()) {
            appendErrors(errors, emailValidator.validate(email));
        }
        // Password: bắt buộc khi insert mới
        String pwd = txtNewPassword.getText();
        boolean isInsert = editing.getId() == 0;
        if (isInsert) {
            if (pwd == null || pwd.isBlank()) {
                appendErrors(errors, List.of("Mật khẩu mới bắt buộc khi tạo user"));
            } else {
                appendErrors(errors, passwordValidator.validate(pwd));
            }
        } else if (pwd != null && !pwd.isBlank()) {
            appendErrors(errors, passwordValidator.validate(pwd));
        }
        if (cbRole.getValue() == null) {
            appendErrors(errors, List.of("Chọn role"));
        }
        if (errors.length() > 0) {
            showError(errors.toString());
            return;
        }

        editing.setUsername(txtUsername.getText().trim());
        editing.setFullName(blankToNull(txtFullName.getText()));
        editing.setEmail(blankToNull(txtEmail.getText()));
        editing.setRole(cbRole.getValue());
        editing.setEnabled(chkEnabled.isSelected());

        try {
            User saved = service.save(editing, pwd);
            if (saved == null) {
                showError("Lưu thất bại — kiểm tra username trùng hoặc constraint role.");
                return;
            }
            editing = saved;
            txtNewPassword.setText("");
            AlertHelper.showInfo("Thành công",
                    "Đã lưu user '" + saved.getUsername() + "' (id=" + saved.getId() + ")");
            reload();
        } catch (Exception e) {
            log.error("Save user fail", e);
            showError("Lưu lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        clearError();
        if (editing == null || editing.getId() == 0) {
            showError("Chọn 1 user trong bảng để xóa");
            return;
        }
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null && current.getId() == editing.getId()) {
            showError("Không thể tự xóa chính mình (admin đang đăng nhập).");
            return;
        }
        boolean ok = AlertHelper.showConfirm("Xác nhận xóa",
                "Xóa user '" + editing.getUsername() + "' (id=" + editing.getId() + ")?\n" +
                        "Tất cả alert_rules.created_by sẽ bị SET NULL.");
        if (!ok) return;
        try {
            if (service.delete(editing.getId())) {
                AlertHelper.showInfo("Đã xóa", "User '" + editing.getUsername() + "' đã xóa.");
                editing = new User();
                clearForm();
                reload();
            } else {
                showError("Xóa thất bại — chỉ ADMIN mới được xóa và không thể xóa chính mình.");
            }
        } catch (Exception e) {
            log.error("Delete user fail", e);
            showError("Xóa lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void handleBackToDashboard() {
        URL fxmlUrl = getClass().getResource(DASHBOARD_FXML);
        if (fxmlUrl == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 800);
            URL css = getClass().getResource(MATERIAL_CSS);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Back to dashboard fail", e);
        }
    }

    private void populateForm(User u) {
        editing = u;
        txtUsername.setText(u.getUsername());
        txtFullName.setText(u.getFullName() == null ? "" : u.getFullName());
        txtEmail.setText(u.getEmail() == null ? "" : u.getEmail());
        cbRole.setValue(u.getRole());
        chkEnabled.setSelected(u.isEnabled());
        txtNewPassword.setText("");
        if (lblMode != null) lblMode.setText("Đang sửa (id=" + u.getId() + ")");
        clearError();
    }

    private void clearForm() {
        txtUsername.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        cbRole.setValue(Role.VIEWER);
        chkEnabled.setSelected(true);
        txtNewPassword.setText("");
        if (lblMode != null) lblMode.setText("Thêm mới");
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void appendErrors(StringBuilder sb, List<String> errors) {
        for (String e : errors) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("• ").append(e);
        }
    }

    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
            lblError.setVisible(true);
        }
    }

    private void clearError() {
        if (lblError != null) lblError.setText("");
    }
}
