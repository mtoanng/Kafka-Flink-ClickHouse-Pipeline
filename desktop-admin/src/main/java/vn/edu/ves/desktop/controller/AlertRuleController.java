package vn.edu.ves.desktop.controller;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.AlertRule;
import vn.edu.ves.desktop.model.MetricType;
import vn.edu.ves.desktop.model.Operator;
import vn.edu.ves.desktop.model.Severity;
import vn.edu.ves.desktop.service.AlertRuleService;
import vn.edu.ves.desktop.service.AlertRuleServiceImpl;
import vn.edu.ves.desktop.util.AlertHelper;
import vn.edu.ves.desktop.util.SessionManager;
import vn.edu.ves.desktop.util.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

/**
 * CRUD AlertRule. Form bên trái + bảng bên phải.
 *
 * <p>Quyền: ADMIN/MANAGER có thể tạo/sửa/xóa; VIEWER chỉ xem.</p>
 */
public class AlertRuleController {

    private static final Logger log = LoggerFactory.getLogger(AlertRuleController.class);
    private static final String DASHBOARD_FXML = "/fxml/dashboard.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";

    @FXML private TextField txtRuleName;
    @FXML private ChoiceBox<MetricType> cbMetricType;
    @FXML private TextField txtFuelType;
    @FXML private TextField txtRegionCode;
    @FXML private TextField txtLocation;
    @FXML private ChoiceBox<Operator> cbOperator;
    @FXML private TextField txtThreshold;
    @FXML private ChoiceBox<Severity> cbSeverity;
    @FXML private CheckBox chkEnabled;
    @FXML private Label lblMode;
    @FXML private Label lblError;

    @FXML private TableView<AlertRule> tblRules;
    @FXML private TableColumn<AlertRule, Long> colId;
    @FXML private TableColumn<AlertRule, String> colName;
    @FXML private TableColumn<AlertRule, MetricType> colMetric;
    @FXML private TableColumn<AlertRule, String> colCondition;
    @FXML private TableColumn<AlertRule, Severity> colSeverity;
    @FXML private TableColumn<AlertRule, Boolean> colEnabled;

    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnToggle;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;

    private final ObservableList<AlertRule> data = FXCollections.observableArrayList();
    private AlertRuleService service = new AlertRuleServiceImpl();
    private AlertRule editing = new AlertRule();

    private final Validator<String> ruleNameValidator = Validator.compose(
            new Validator.NotBlankValidator("Rule name"),
            new Validator.LengthRangeValidator("Rule name", 3, 150));
    private final Validator<String> thresholdValidator = Validator.compose(
            new Validator.NotBlankValidator("Threshold"),
            new Validator.PatternValidator("Threshold",
                    "^-?\\d+(\\.\\d+)?$", "phải là số (vd: 95, 100.5)"));

    public void setService(AlertRuleService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        configureTable();
        cbMetricType.setItems(FXCollections.observableArrayList(MetricType.values()));
        cbMetricType.getSelectionModel().select(MetricType.FUEL_PRICE);
        cbOperator.setItems(FXCollections.observableArrayList(Operator.values()));
        cbOperator.getSelectionModel().select(Operator.GT);
        cbSeverity.setItems(FXCollections.observableArrayList(Severity.values()));
        cbSeverity.getSelectionModel().select(Severity.WARNING);
        applyPermissions();
        clearForm();
        reload();
        tblRules.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) populateForm(newV);
        });
    }

    private void configureTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("ruleName"));
        colMetric.setCellValueFactory(new PropertyValueFactory<>("metricType"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("displayCondition"));
        colSeverity.setCellValueFactory(new PropertyValueFactory<>("severity"));
        colEnabled.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        tblRules.setItems(data);
    }

    private void applyPermissions() {
        boolean canWrite = SessionManager.getInstance().canWrite();
        for (javafx.scene.Node n : new javafx.scene.Node[]{txtRuleName, cbMetricType, txtFuelType,
                txtRegionCode, txtLocation, cbOperator, txtThreshold, cbSeverity, chkEnabled}) {
            if (n != null) n.setDisable(!canWrite);
        }
        if (btnNew != null) btnNew.setDisable(!canWrite);
        if (btnSave != null) btnSave.setDisable(!canWrite);
        if (btnDelete != null) btnDelete.setDisable(!canWrite);
        if (btnToggle != null) btnToggle.setDisable(!canWrite);
    }

    @FXML
    public void reload() {
        clearError();
        try {
            List<AlertRule> all = service.findAll();
            data.setAll(all);
        } catch (Exception e) {
            log.error("Reload alert_rules fail", e);
            showError("Không tải được danh sách alert_rules: " + e.getMessage());
        }
    }

    @FXML
    public void handleNew() {
        editing = new AlertRule();
        clearForm();
        if (lblMode != null) lblMode.setText("Thêm mới");
    }

    @FXML
    public void handleSave() {
        clearError();
        StringBuilder errors = new StringBuilder();
        appendErrors(errors, ruleNameValidator.validate(txtRuleName.getText()));
        appendErrors(errors, thresholdValidator.validate(txtThreshold.getText()));
        if (cbMetricType.getValue() == null) appendErrors(errors, List.of("Chọn metric_type"));
        if (cbOperator.getValue() == null) appendErrors(errors, List.of("Chọn operator"));
        if (cbSeverity.getValue() == null) appendErrors(errors, List.of("Chọn severity"));
        if (errors.length() > 0) {
            showError(errors.toString());
            return;
        }

        editing.setRuleName(txtRuleName.getText().trim());
        editing.setMetricType(cbMetricType.getValue());
        editing.setFuelType(blankToNull(txtFuelType.getText()));
        editing.setRegionCode(blankToNull(txtRegionCode.getText()));
        editing.setLocation(blankToNull(txtLocation.getText()));
        editing.setOperator(cbOperator.getValue());
        editing.setThreshold(new BigDecimal(txtThreshold.getText().trim()));
        editing.setSeverity(cbSeverity.getValue());
        editing.setEnabled(chkEnabled.isSelected());
        if (editing.getId() == 0 && SessionManager.getInstance().getCurrentUser() != null) {
            editing.setCreatedBy(SessionManager.getInstance().getCurrentUser().getId());
        }

        try {
            AlertRule saved = service.save(editing);
            if (saved == null) {
                showError("Lưu thất bại — kiểm tra constraint (metric_type/operator/severity).");
                return;
            }
            editing = saved;
            AlertHelper.showInfo("Thành công",
                    "Đã lưu rule '" + saved.getRuleName() + "' (id=" + saved.getId() + ")");
            reload();
        } catch (Exception e) {
            log.error("Save alert_rule fail", e);
            showError("Lưu lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        clearError();
        if (editing == null || editing.getId() == 0) {
            showError("Chọn 1 rule trong bảng để xóa");
            return;
        }
        boolean ok = AlertHelper.showConfirm("Xác nhận",
                "Xóa rule '" + editing.getRuleName() + "'?\n" +
                        "Tất cả alerts đã sinh từ rule này sẽ CASCADE xóa.");
        if (!ok) return;
        try {
            if (service.delete(editing.getId())) {
                AlertHelper.showInfo("Đã xóa", "Rule '" + editing.getRuleName() + "' đã xóa.");
                editing = new AlertRule();
                clearForm();
                reload();
            } else {
                showError("Xóa thất bại — kiểm tra log.");
            }
        } catch (Exception e) {
            log.error("Delete alert_rule fail", e);
            showError("Xóa lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void handleToggleEnabled() {
        clearError();
        if (editing == null || editing.getId() == 0) {
            showError("Chọn 1 rule trong bảng để toggle enabled");
            return;
        }
        boolean newValue = !editing.isEnabled();
        try {
            if (service.setEnabled(editing.getId(), newValue)) {
                editing.setEnabled(newValue);
                chkEnabled.setSelected(newValue);
                reload();
                AlertHelper.showInfo("OK",
                        "Rule '" + editing.getRuleName() + "' đã " + (newValue ? "BẬT" : "TẮT"));
            } else {
                showError("Toggle thất bại — kiểm tra log.");
            }
        } catch (Exception e) {
            log.error("Toggle enabled fail", e);
            showError("Lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void handleBackToDashboard() {
        URL fxmlUrl = getClass().getResource(DASHBOARD_FXML);
        if (fxmlUrl == null) {
            AlertHelper.showWarning("Lỗi", "Không tìm thấy dashboard.fxml");
            return;
        }
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

    private void populateForm(AlertRule r) {
        editing = r;
        txtRuleName.setText(r.getRuleName());
        cbMetricType.setValue(r.getMetricType());
        txtFuelType.setText(r.getFuelType() == null ? "" : r.getFuelType());
        txtRegionCode.setText(r.getRegionCode() == null ? "" : r.getRegionCode());
        txtLocation.setText(r.getLocation() == null ? "" : r.getLocation());
        cbOperator.setValue(r.getOperator());
        txtThreshold.setText(r.getThreshold() == null ? "" : r.getThreshold().toPlainString());
        cbSeverity.setValue(r.getSeverity());
        chkEnabled.setSelected(r.isEnabled());
        if (lblMode != null) lblMode.setText("Đang sửa (id=" + r.getId() + ")");
        clearError();
    }

    private void clearForm() {
        txtRuleName.setText("");
        cbMetricType.setValue(MetricType.FUEL_PRICE);
        txtFuelType.setText("");
        txtRegionCode.setText("");
        txtLocation.setText("");
        cbOperator.setValue(Operator.GT);
        txtThreshold.setText("");
        cbSeverity.setValue(Severity.WARNING);
        chkEnabled.setSelected(true);
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
