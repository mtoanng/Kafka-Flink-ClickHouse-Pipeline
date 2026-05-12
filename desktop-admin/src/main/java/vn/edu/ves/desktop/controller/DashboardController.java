package vn.edu.ves.desktop.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.Pillar1Outlook;
import vn.edu.ves.desktop.model.Pillar2Volatility;
import vn.edu.ves.desktop.model.Pillar3Shedding;
import vn.edu.ves.desktop.model.Pillar4NetZero;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.service.AuthService;
import vn.edu.ves.desktop.service.AuthServiceImpl;
import vn.edu.ves.desktop.service.DashboardService;
import vn.edu.ves.desktop.service.DashboardServiceImpl;
import vn.edu.ves.desktop.util.AlertHelper;
import vn.edu.ves.desktop.util.SessionManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller cho <code>fxml/dashboard.fxml</code>.
 *
 * <p>Trách nhiệm Phase 5.2:</p>
 * <ul>
 *   <li>Top bar: Security Score gauge (ProgressIndicator) + status + tên user + nav buttons.</li>
 *   <li>TabPane 4 pillar: mỗi tab có TableView + chart minh họa.</li>
 *   <li>Sidebar phải: ListView recommendations active (đọc <code>v_active_recommendations</code>).</li>
 *   <li>Auto-refresh 30s qua ScheduledExecutorService, runLater để update UI.</li>
 *   <li>Nav buttons: Regions / AlertRules / Users (Users chỉ visible khi admin).</li>
 * </ul>
 */
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final String LOGIN_FXML = "/fxml/login.fxml";
    private static final String REGION_FXML = "/fxml/region.fxml";
    private static final String ALERT_RULE_FXML = "/fxml/alertRule.fxml";
    private static final String USER_FXML = "/fxml/user.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";
    private static final long REFRESH_INTERVAL_SEC = 30L;

    // ---- top bar ----
    @FXML private Label lblUserInfo;
    @FXML private Label lblOverallScore;
    @FXML private Label lblScoreStatus;
    @FXML private ProgressIndicator scoreGauge;
    @FXML private Label lblLastRefresh;
    @FXML private Button btnRefresh;
    @FXML private Button btnRegions;
    @FXML private Button btnAlertRules;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;

    // ---- pillar 1 ----
    @FXML private TableView<Pillar1Outlook> tblPillar1;
    @FXML private TableColumn<Pillar1Outlook, String> colP1Region;
    @FXML private TableColumn<Pillar1Outlook, String> colP1Fuel;
    @FXML private TableColumn<Pillar1Outlook, BigDecimal> colP1StockDays;
    @FXML private TableColumn<Pillar1Outlook, Integer> colP1Target;
    @FXML private TableColumn<Pillar1Outlook, String> colP1Status;
    @FXML private TableColumn<Pillar1Outlook, String> colP1Rec;
    @FXML private BarChart<String, Number> chartPillar1;

    // ---- pillar 2 ----
    @FXML private TableView<Pillar2Volatility> tblPillar2;
    @FXML private TableColumn<Pillar2Volatility, String> colP2Fuel;
    @FXML private TableColumn<Pillar2Volatility, String> colP2Loc;
    @FXML private TableColumn<Pillar2Volatility, BigDecimal> colP2Avg;
    @FXML private TableColumn<Pillar2Volatility, BigDecimal> colP2Sigma;
    @FXML private TableColumn<Pillar2Volatility, BigDecimal> colP2RelVol;
    @FXML private TableColumn<Pillar2Volatility, String> colP2Signal;
    @FXML private LineChart<String, Number> chartPillar2;

    // ---- pillar 3 ----
    @FXML private TableView<Pillar3Shedding> tblPillar3;
    @FXML private TableColumn<Pillar3Shedding, String> colP3Region;
    @FXML private TableColumn<Pillar3Shedding, BigDecimal> colP3LoadMw;
    @FXML private TableColumn<Pillar3Shedding, BigDecimal> colP3CapMw;
    @FXML private TableColumn<Pillar3Shedding, BigDecimal> colP3LoadPct;
    @FXML private TableColumn<Pillar3Shedding, String> colP3Action;
    @FXML private TableColumn<Pillar3Shedding, BigDecimal> colP3Shed;
    @FXML private BarChart<String, Number> chartPillar3;

    // ---- pillar 4 ----
    @FXML private TableView<Pillar4NetZero> tblPillar4;
    @FXML private TableColumn<Pillar4NetZero, String> colP4Region;
    @FXML private TableColumn<Pillar4NetZero, BigDecimal> colP4Renew;
    @FXML private TableColumn<Pillar4NetZero, BigDecimal> colP4Load;
    @FXML private TableColumn<Pillar4NetZero, BigDecimal> colP4SharePct;
    @FXML private TableColumn<Pillar4NetZero, String> colP4Status;
    @FXML private PieChart chartPillar4;

    // ---- sidebar ----
    @FXML private ListView<Recommendation> lstRecommendations;
    @FXML private Label lblRecCount;

    private final ObservableList<Pillar1Outlook> p1Data = FXCollections.observableArrayList();
    private final ObservableList<Pillar2Volatility> p2Data = FXCollections.observableArrayList();
    private final ObservableList<Pillar3Shedding> p3Data = FXCollections.observableArrayList();
    private final ObservableList<Pillar4NetZero> p4Data = FXCollections.observableArrayList();
    private final ObservableList<Recommendation> recData = FXCollections.observableArrayList();

    private DashboardService dashboardService = new DashboardServiceImpl();
    private final AuthService authService = new AuthServiceImpl();
    private ScheduledExecutorService scheduler;

    /** Setter cho test. */
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @FXML
    public void initialize() {
        configureTopBar();
        configurePillar1();
        configurePillar2();
        configurePillar3();
        configurePillar4();
        configureRecommendations();
        startAutoRefresh();
    }

    private void configureTopBar() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u != null) {
            String label = (u.getFullName() == null || u.getFullName().isBlank()
                    ? u.getUsername() : u.getFullName()) + " — " + u.getRole();
            if (lblUserInfo != null) lblUserInfo.setText(label);
        }
        if (btnUsers != null) {
            boolean isAdmin = SessionManager.getInstance().isAdmin();
            btnUsers.setVisible(isAdmin);
            btnUsers.setManaged(isAdmin);
        }
    }

    private void configurePillar1() {
        if (tblPillar1 == null) return;
        colP1Region.setCellValueFactory(new PropertyValueFactory<>("regionCode"));
        colP1Fuel.setCellValueFactory(new PropertyValueFactory<>("fuelType"));
        colP1StockDays.setCellValueFactory(new PropertyValueFactory<>("stockDays"));
        colP1Target.setCellValueFactory(new PropertyValueFactory<>("targetDays"));
        colP1Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        colP1Rec.setCellValueFactory(new PropertyValueFactory<>("recommendationText"));
        tblPillar1.setItems(p1Data);
    }

    private void configurePillar2() {
        if (tblPillar2 == null) return;
        colP2Fuel.setCellValueFactory(new PropertyValueFactory<>("fuelType"));
        colP2Loc.setCellValueFactory(new PropertyValueFactory<>("location"));
        colP2Avg.setCellValueFactory(new PropertyValueFactory<>("avgPrice"));
        colP2Sigma.setCellValueFactory(new PropertyValueFactory<>("sigma"));
        colP2RelVol.setCellValueFactory(new PropertyValueFactory<>("relativeVolatilityPct"));
        colP2Signal.setCellValueFactory(new PropertyValueFactory<>("signal"));
        tblPillar2.setItems(p2Data);
    }

    private void configurePillar3() {
        if (tblPillar3 == null) return;
        colP3Region.setCellValueFactory(new PropertyValueFactory<>("regionCode"));
        colP3LoadMw.setCellValueFactory(new PropertyValueFactory<>("loadMw"));
        colP3CapMw.setCellValueFactory(new PropertyValueFactory<>("capacityMw"));
        colP3LoadPct.setCellValueFactory(new PropertyValueFactory<>("loadPct"));
        colP3Action.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        colP3Shed.setCellValueFactory(new PropertyValueFactory<>("suggestedShedMw"));
        tblPillar3.setItems(p3Data);
    }

    private void configurePillar4() {
        if (tblPillar4 == null) return;
        colP4Region.setCellValueFactory(new PropertyValueFactory<>("regionCode"));
        colP4Renew.setCellValueFactory(new PropertyValueFactory<>("renewableMw"));
        colP4Load.setCellValueFactory(new PropertyValueFactory<>("avgLoadMw"));
        colP4SharePct.setCellValueFactory(new PropertyValueFactory<>("currentRenewableSharePct"));
        colP4Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblPillar4.setItems(p4Data);
    }

    private void configureRecommendations() {
        if (lstRecommendations == null) return;
        lstRecommendations.setItems(recData);
        lstRecommendations.setCellFactory(list -> new javafx.scene.control.ListCell<Recommendation>() {
            @Override
            protected void updateItem(Recommendation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String prefix = item.getSeverity() == null ? "" : "[" + item.getSeverity() + "] ";
                    setText(prefix + (item.getTitle() == null ? "" : item.getTitle()));
                    setStyle(severityStyle(item.getSeverity()));
                }
            }
        });
    }

    private String severityStyle(String severity) {
        if (severity == null) return "";
        switch (severity) {
            case "CRITICAL":
                return "-fx-text-fill: #cc0000; -fx-font-weight: 600;";
            case "WARNING":
                return "-fx-text-fill: #b45f06; -fx-font-weight: 600;";
            default:
                return "-fx-text-fill: #455a64;";
        }
    }

    private void startAutoRefresh() {
        refresh();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "dashboard-refresh");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::refresh,
                REFRESH_INTERVAL_SEC, REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    @FXML
    public void refresh() {
        Task<DashboardSnapshot> task = new Task<DashboardSnapshot>() {
            @Override
            protected DashboardSnapshot call() {
                DashboardSnapshot snap = new DashboardSnapshot();
                snap.score = dashboardService.getSecurityScore().orElse(null);
                snap.p1 = dashboardService.getPillar1();
                snap.p2 = dashboardService.getPillar2();
                snap.p3 = dashboardService.getPillar3();
                snap.p4 = dashboardService.getPillar4();
                snap.recs = dashboardService.getActiveRecommendations();
                return snap;
            }
        };
        task.setOnSucceeded(e -> applySnapshot(task.getValue()));
        task.setOnFailed(e -> {
            log.error("Refresh dashboard fail", task.getException());
            Platform.runLater(() -> AlertHelper.showWarning("Refresh fail",
                    "Không tải được dữ liệu dashboard: " + task.getException().getMessage()));
        });
        Thread t = new Thread(task, "dashboard-refresh-task");
        t.setDaemon(true);
        t.start();
    }

    private void applySnapshot(DashboardSnapshot snap) {
        applyScore(snap.score);
        p1Data.setAll(snap.p1 == null ? List.of() : snap.p1);
        p2Data.setAll(snap.p2 == null ? List.of() : snap.p2);
        p3Data.setAll(snap.p3 == null ? List.of() : snap.p3);
        p4Data.setAll(snap.p4 == null ? List.of() : snap.p4);
        recData.setAll(snap.recs == null ? List.of() : snap.recs);
        refreshPillar1Chart(snap.p1);
        refreshPillar2Chart(snap.p2);
        refreshPillar3Chart(snap.p3);
        refreshPillar4Chart(snap.p4);
        if (lblRecCount != null) {
            lblRecCount.setText(recData.size() + " active");
        }
        if (lblLastRefresh != null) {
            lblLastRefresh.setText("Last refresh: " + java.time.LocalTime.now().withNano(0));
        }
    }

    private void applyScore(SecurityScore score) {
        if (score == null) {
            if (lblOverallScore != null) lblOverallScore.setText("--");
            if (lblScoreStatus != null) lblScoreStatus.setText("NO_DATA");
            if (scoreGauge != null) scoreGauge.setProgress(0);
            return;
        }
        if (lblOverallScore != null && score.getOverallScore() != null) {
            lblOverallScore.setText(score.getOverallScore().setScale(1, RoundingMode.HALF_UP).toPlainString());
        }
        if (lblScoreStatus != null) {
            lblScoreStatus.setText(score.getStatus() == null ? "" : score.getStatus());
            lblScoreStatus.getStyleClass().removeAll("status-secure", "status-stable", "status-at-risk", "status-critical");
            String css = scoreStatusStyle(score.getStatus());
            if (css != null) lblScoreStatus.getStyleClass().add(css);
        }
        if (scoreGauge != null) {
            scoreGauge.setProgress(score.getOverallScoreRatio());
        }
    }

    private String scoreStatusStyle(String status) {
        if (status == null) return null;
        switch (status) {
            case "SECURE": return "status-secure";
            case "STABLE": return "status-stable";
            case "AT_RISK": return "status-at-risk";
            case "CRITICAL": return "status-critical";
            default: return null;
        }
    }

    private void refreshPillar1Chart(List<Pillar1Outlook> rows) {
        if (chartPillar1 == null) return;
        chartPillar1.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Stock days (per region/fuel)");
        for (Pillar1Outlook r : rows) {
            String label = (r.getRegionCode() == null ? "?" : r.getRegionCode()) + "/" +
                    (r.getFuelType() == null ? "?" : r.getFuelType());
            Number val = r.getStockDays() == null ? 0 : r.getStockDays();
            series.getData().add(new XYChart.Data<>(label, val));
        }
        chartPillar1.getData().add(series);
    }

    private void refreshPillar2Chart(List<Pillar2Volatility> rows) {
        if (chartPillar2 == null) return;
        chartPillar2.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg price (1h window)");
        for (Pillar2Volatility r : rows) {
            String label = (r.getFuelType() == null ? "?" : r.getFuelType()) +
                    (r.getLocation() == null ? "" : "@" + r.getLocation());
            Number val = r.getAvgPrice() == null ? 0 : r.getAvgPrice();
            series.getData().add(new XYChart.Data<>(label, val));
        }
        chartPillar2.getData().add(series);
    }

    private void refreshPillar3Chart(List<Pillar3Shedding> rows) {
        if (chartPillar3 == null) return;
        chartPillar3.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Load %");
        for (Pillar3Shedding r : rows) {
            String label = r.getRegionCode() == null ? "?" : r.getRegionCode();
            Number val = r.getLoadPct() == null ? 0 : r.getLoadPct();
            XYChart.Data<String, Number> d = new XYChart.Data<>(label, val);
            series.getData().add(d);
        }
        chartPillar3.getData().add(series);
        Platform.runLater(() -> colorPillar3Bars(rows));
    }

    private void colorPillar3Bars(List<Pillar3Shedding> rows) {
        if (chartPillar3.getData().isEmpty()) return;
        XYChart.Series<String, Number> series = chartPillar3.getData().get(0);
        int idx = 0;
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() == null) continue;
            Pillar3Shedding row = idx < rows.size() ? rows.get(idx) : null;
            String style = "-fx-bar-fill: #1976d2;";
            if (row != null && row.getLoadPct() != null) {
                double pct = row.getLoadPct().doubleValue();
                if (pct >= 95) style = "-fx-bar-fill: #cc0000;";
                else if (pct >= 85) style = "-fx-bar-fill: #b45f06;";
                else if (pct >= 70) style = "-fx-bar-fill: #f1c232;";
            }
            d.getNode().setStyle(style);
            idx++;
        }
    }

    private void refreshPillar4Chart(List<Pillar4NetZero> rows) {
        if (chartPillar4 == null) return;
        chartPillar4.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        for (Pillar4NetZero r : rows) {
            BigDecimal share = r.getCurrentRenewableSharePct();
            if (share == null) continue;
            String label = (r.getRegionCode() == null ? "?" : r.getRegionCode()) + " (" +
                    share.setScale(1, RoundingMode.HALF_UP) + "%)";
            chartPillar4.getData().add(new PieChart.Data(label, share.doubleValue()));
        }
    }

    @FXML
    public void handleLogout() {
        boolean ok = AlertHelper.showConfirm("Đăng xuất", "Bạn có chắc muốn đăng xuất?");
        if (!ok) return;
        shutdownScheduler();
        authService.logout();
        switchScene(LOGIN_FXML, 480, 360);
    }

    @FXML
    public void handleOpenRegions() {
        switchScene(REGION_FXML, 1280, 800);
    }

    @FXML
    public void handleOpenAlertRules() {
        switchScene(ALERT_RULE_FXML, 1280, 800);
    }

    @FXML
    public void handleOpenUsers() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertHelper.showWarning("Không có quyền", "Chỉ ADMIN mới có thể quản lý user.");
            return;
        }
        switchScene(USER_FXML, 1280, 800);
    }

    private void switchScene(String fxmlPath, int width, int height) {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            AlertHelper.showInfo("Chưa sẵn sàng",
                    "Màn hình " + fxmlPath + " sẽ có ở phase sau.");
            return;
        }
        try {
            shutdownScheduler();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            URL css = getClass().getResource(MATERIAL_CSS);
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            log.error("Load FXML {} fail", fxmlPath, e);
            AlertHelper.showError("Lỗi", "Không mở được màn hình: " + e.getMessage());
        }
    }

    /** Gọi từ MainApp khi window close để giải phóng scheduler. */
    public void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /** Snapshot tổng hợp data nhặt trong background thread, apply atomic ở UI thread. */
    private static class DashboardSnapshot {
        SecurityScore score;
        List<Pillar1Outlook> p1;
        List<Pillar2Volatility> p2;
        List<Pillar3Shedding> p3;
        List<Pillar4NetZero> p4;
        List<Recommendation> recs;
    }
}
