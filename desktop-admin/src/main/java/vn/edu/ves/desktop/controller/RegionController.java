package vn.edu.ves.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.Region;
import vn.edu.ves.desktop.service.RegionService;
import vn.edu.ves.desktop.service.RegionServiceImpl;
import vn.edu.ves.desktop.util.AlertHelper;
import vn.edu.ves.desktop.util.SessionManager;
import vn.edu.ves.desktop.util.Validator;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * CRUD region: left form + right TableView.
 *
 * <p>Quyền ghi: ADMIN/MANAGER. VIEWER chỉ xem (form disabled).</p>
 */
public class RegionController {

    private static final Logger log = LoggerFactory.getLogger(RegionController.class);
    private static final String DASHBOARD_FXML = "/fxml/dashboard.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtCountry;
    @FXML private ChoiceBox<String> cbVnZone;
    @FXML private TextArea txtDescription;
    @FXML private Label lblError;
    @FXML private Label lblMode;

    @FXML private TableView<Region> tblRegions;
    @FXML private TableColumn<Region, Long> colId;
    @FXML private TableColumn<Region, String> colCode;
    @FXML private TableColumn<Region, String> colName;
    @FXML private TableColumn<Region, String> colZone;
    @FXML private TableColumn<Region, String> colCountry;

    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;

    private final ObservableList<Region> data = FXCollections.observableArrayList();
    private RegionService service = new RegionServiceImpl();
    private Region editing = new Region();

    /** Validator chain reuse Strategy pattern: NotBlank + LengthRange + Pattern + InSet. */
    private final Validator<String> codeValidator = Validator.compose(
            new Validator.NotBlankValidator("Mã region"),
            new Validator.LengthRangeValidator("Mã region", 2, 20),
            new Validator.PatternValidator("Mã region", "^[A-Z0-9_]+$",
                    "chỉ chứa chữ HOA, số, hoặc dấu gạch dưới"));
    private final Validator<String> nameValidator = Validator.compose(
            new Validator.NotBlankValidator("Tên region"),
            new Validator.LengthRangeValidator("Tên region", 2, 100));
    private final Validator<String> countryValidator = Validator.compose(
            new Validator.NotBlankValidator("Country code"),
            new Validator.LengthRangeValidator("Country code", 2, 3),
            new Validator.PatternValidator("Country code", "^[A-Z]+$",
                    "viết HOA toàn bộ (ISO 3166)"));
    private final Validator<String> zoneValidator = new Validator.InSetValidator(
            "VN zone", "BAC", "TRUNG", "NAM", "");

    public void setService(RegionService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        configureTable();
        cbVnZone.setItems(FXCollections.observableArrayList("", "BAC", "TRUNG", "NAM"));
        cbVnZone.getSelectionModel().select(0);
        applyPermissions();
        clearForm();
        reload();
        tblRegions.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) populateForm(newV);
        });
    }

    private void configureTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colZone.setCellValueFactory(new PropertyValueFactory<>("vnZone"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("countryCode"));
        tblRegions.setItems(data);
    }

    private void applyPermissions() {
        boolean canWrite = SessionManager.getInstance().canWrite();
        for (javafx.scene.Node n : new javafx.scene.Node[]{txtCode, txtName, txtCountry, cbVnZone, txtDescription}) {
            if (n != null) n.setDisable(!canWrite);
        }
        if (btnNew != null) btnNew.setDisable(!canWrite);
        if (btnSave != null) btnSave.setDisable(!canWrite);
        if (btnDelete != null) btnDelete.setDisable(!canWrite);
    }

    @FXML
    public void reload() {
        clearError();
        try {
            List<Region> all = service.findAll();
            data.setAll(all);
        } catch (Exception e) {
            log.error("Reload regions fail", e);
            showError("Không tải được danh sách region: " + e.getMessage());
        }
    }

    @FXML
    public void handleNew() {
        editing = new Region();
        clearForm();
        if (lblMode != null) lblMode.setText("Thêm mới");
        if (txtCode != null) txtCode.requestFocus();
    }

    @FXML
    public void handleSave() {
        clearError();
        String code = txtCode.getText();
        String name = txtName.getText();
        String country = txtCountry.getText();
        String zone = cbVnZone.getValue();
        String desc = txtDescription.getText();

        StringBuilder errors = new StringBuilder();
        appendErrors(errors, codeValidator.validate(code));
        appendErrors(errors, nameValidator.validate(name));
        appendErrors(errors, countryValidator.validate(country));
        appendErrors(errors, zoneValidator.validate(zone));
        if (errors.length() > 0) {
            showError(errors.toString());
            return;
        }

        editing.setCode(code.trim().toUpperCase());
        editing.setName(name.trim());
        editing.setCountryCode(country.trim().toUpperCase());
        editing.setVnZone(zone == null || zone.isBlank() ? null : zone);
        editing.setDescription(desc == null || desc.isBlank() ? null : desc.trim());

        try {
            Region saved = service.save(editing);
            if (saved == null) {
                showError("Lưu thất bại (kiểm tra log + DB).");
                return;
            }
            editing = saved;
            AlertHelper.showInfo("Thành công",
                    "Đã lưu region " + saved.getCode() + " (id=" + saved.getId() + ")");
            reload();
        } catch (Exception e) {
            log.error("Save region fail", e);
            showError("Lưu lỗi: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        clearError();
        if (editing == null || editing.getId() == 0) {
            showError("Chọn 1 region trong bảng để xóa");
            return;
        }
        boolean ok = AlertHelper.showConfirm("Xác nhận",
                "Xóa region " + editing.getCode() + " (id=" + editing.getId() + ")?\n" +
                        "Hành động này không thể hoàn tác.");
        if (!ok) return;
        try {
            boolean deleted = service.delete(editing.getId());
            if (deleted) {
                AlertHelper.showInfo("Đã xóa", "Region " + editing.getCode() + " đã xóa.");
                editing = new Region();
                clearForm();
                reload();
            } else {
                showError("Xóa thất bại — có thể có ràng buộc FK (alert_rules, fuel_inventory_raw...).");
            }
        } catch (Exception e) {
            log.error("Delete region fail", e);
            showError("Xóa lỗi: " + e.getMessage());
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

    /* --------- internals --------- */

    private void populateForm(Region r) {
        editing = r;
        txtCode.setText(r.getCode());
        txtName.setText(r.getName());
        txtCountry.setText(r.getCountryCode() == null ? "VN" : r.getCountryCode());
        cbVnZone.getSelectionModel().select(r.getVnZone() == null ? "" : r.getVnZone());
        txtDescription.setText(r.getDescription() == null ? "" : r.getDescription());
        if (lblMode != null) lblMode.setText("Đang sửa (id=" + r.getId() + ")");
        clearError();
    }

    private void clearForm() {
        if (txtCode != null) txtCode.setText("");
        if (txtName != null) txtName.setText("");
        if (txtCountry != null) txtCountry.setText("VN");
        if (cbVnZone != null) cbVnZone.getSelectionModel().select(0);
        if (txtDescription != null) txtDescription.setText("");
        if (lblMode != null) lblMode.setText("Thêm mới");
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
