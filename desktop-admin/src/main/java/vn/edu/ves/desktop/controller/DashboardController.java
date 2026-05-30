package vn.edu.ves.desktop.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.Pillar1SupplySecurity;
import vn.edu.ves.desktop.model.Pillar2MarketResilience;
import vn.edu.ves.desktop.model.Pillar3GridReliability;
import vn.edu.ves.desktop.model.Pillar4EnergyTransition;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.service.AuthService;
import vn.edu.ves.desktop.service.AuthServiceImpl;
import vn.edu.ves.desktop.service.DashboardService;
import vn.edu.ves.desktop.service.DashboardServiceImpl;
import vn.edu.ves.desktop.service.LiveMetricsService;
import vn.edu.ves.desktop.service.LiveMetricsServiceImpl;
import vn.edu.ves.desktop.util.AlertHelper;
import vn.edu.ves.desktop.util.SessionManager;
import vn.edu.ves.desktop.widget.PulseEffect;
import vn.edu.ves.desktop.widget.Sparkline;
import vn.edu.ves.desktop.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller cho <code>fxml/dashboard.fxml</code>.
 *
 * <p>Phase 7.1 — wired to the IEA / APERC pillar taxonomy:</p>
 * <ul>
 *   <li>Pillar 1 — Supply Security (IDR / SFRI / HHI / N-1).</li>
 *   <li>Pillar 2 — Market Resilience (σ30d / price gap / β crude / affordability).</li>
 *   <li>Pillar 3 — Grid Reliability (reserve margin / peak factor / shed prob / freq).</li>
 *   <li>Pillar 4 — Energy Transition (renewable% / CO2 intensity / curtailment / netzero).</li>
 * </ul>
 *
 * <p>Composite ESI = 0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4 drives the top-bar gauge.</p>
 */
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final String LOGIN_FXML = "/fxml/login.fxml";
    private static final String REGION_FXML = "/fxml/region.fxml";
    private static final String ALERT_RULE_FXML = "/fxml/alertRule.fxml";
    private static final String USER_FXML = "/fxml/user.fxml";
    private static final String MATERIAL_CSS = "/css/material.css";
    /** Default table-cadence refresh (overridden by ui.refresh.table.s). */
    private static final long REFRESH_INTERVAL_SEC = 10L;

    // ---- top bar ----
    @FXML private Label lblUserInfo;
    @FXML private Label lblOverallScore;
    @FXML private Label lblScoreStatus;
    @FXML private ProgressIndicator scoreGauge;
    @FXML private Label lblLastRefresh;
    @FXML private Label lblLiveTicker;
    @FXML private Button btnRefresh;
    @FXML private Button btnRegions;
    @FXML private Button btnAlertRules;
    @FXML private Button btnUsers;
    @FXML private Button btnLogout;

    // ---- pillar 1 — Supply Security ----
    @FXML private TableView<Pillar1SupplySecurity> tblPillar1;
    @FXML private TableColumn<Pillar1SupplySecurity, String> colP1Region;
    @FXML private TableColumn<Pillar1SupplySecurity, String> colP1Fuel;
    @FXML private TableColumn<Pillar1SupplySecurity, BigDecimal> colP1Idr;
    @FXML private TableColumn<Pillar1SupplySecurity, BigDecimal> colP1Sfri;
    @FXML private TableColumn<Pillar1SupplySecurity, BigDecimal> colP1Hhi;
    @FXML private TableColumn<Pillar1SupplySecurity, BigDecimal> colP1N1;
    @FXML private TableColumn<Pillar1SupplySecurity, BigDecimal> colP1Score;
    @FXML private TableColumn<Pillar1SupplySecurity, String> colP1Status;
    @FXML private BarChart<String, Number> chartPillar1;
    @FXML private Sparkline sparkP1;

    // ---- pillar 2 — Market Resilience ----
    @FXML private TableView<Pillar2MarketResilience> tblPillar2;
    @FXML private TableColumn<Pillar2MarketResilience, String> colP2Fuel;
    @FXML private TableColumn<Pillar2MarketResilience, BigDecimal> colP2Sigma;
    @FXML private TableColumn<Pillar2MarketResilience, BigDecimal> colP2Gap;
    @FXML private TableColumn<Pillar2MarketResilience, BigDecimal> colP2Beta;
    @FXML private TableColumn<Pillar2MarketResilience, BigDecimal> colP2Afford;
    @FXML private TableColumn<Pillar2MarketResilience, BigDecimal> colP2Score;
    @FXML private TableColumn<Pillar2MarketResilience, String> colP2Status;
    @FXML private LineChart<String, Number> chartPillar2;
    @FXML private Sparkline sparkP2;

    // ---- pillar 3 — Grid Reliability ----
    @FXML private TableView<Pillar3GridReliability> tblPillar3;
    @FXML private TableColumn<Pillar3GridReliability, String> colP3Region;
    @FXML private TableColumn<Pillar3GridReliability, BigDecimal> colP3Reserve;
    @FXML private TableColumn<Pillar3GridReliability, BigDecimal> colP3Peak;
    @FXML private TableColumn<Pillar3GridReliability, BigDecimal> colP3Shed;
    @FXML private TableColumn<Pillar3GridReliability, BigDecimal> colP3Freq;
    @FXML private TableColumn<Pillar3GridReliability, BigDecimal> colP3Score;
    @FXML private TableColumn<Pillar3GridReliability, String> colP3Status;
    @FXML private BarChart<String, Number> chartPillar3;
    @FXML private Sparkline sparkP3;

    // ---- pillar 4 — Energy Transition ----
    @FXML private TableView<Pillar4EnergyTransition> tblPillar4;
    @FXML private TableColumn<Pillar4EnergyTransition, String> colP4Region;
    @FXML private TableColumn<Pillar4EnergyTransition, BigDecimal> colP4Renew;
    @FXML private TableColumn<Pillar4EnergyTransition, BigDecimal> colP4Co2;
    @FXML private TableColumn<Pillar4EnergyTransition, BigDecimal> colP4Curtail;
    @FXML private TableColumn<Pillar4EnergyTransition, BigDecimal> colP4Netzero;
    @FXML private TableColumn<Pillar4EnergyTransition, BigDecimal> colP4Score;
    @FXML private TableColumn<Pillar4EnergyTransition, String> colP4Status;
    @FXML private PieChart chartPillar4;
    @FXML private Sparkline sparkP4;

    // ---- maps tab (Phase 7.3) ----
    @FXML private javafx.scene.layout.StackPane mapsContainer;
    private MapsController mapsController;

    // ---- sidebar ----
    @FXML private ListView<Recommendation> lstRecommendations;
    @FXML private Label lblRecCount;

    private final ObservableList<Pillar1SupplySecurity> p1Data = FXCollections.observableArrayList();
    private final ObservableList<Pillar2MarketResilience> p2Data = FXCollections.observableArrayList();
    private final ObservableList<Pillar3GridReliability> p3Data = FXCollections.observableArrayList();
    private final ObservableList<Pillar4EnergyTransition> p4Data = FXCollections.observableArrayList();
    private final ObservableList<Recommendation> recData = FXCollections.observableArrayList();

    private DashboardService dashboardService = new DashboardServiceImpl();
    private final AuthService authService = new AuthServiceImpl();
    private ScheduledExecutorService scheduler;

    // Phase 7.2 — live metrics + alert tracking
    private LiveMetricsService liveMetrics = new LiveMetricsServiceImpl();
    private final Set<Long> knownCriticalAlertIds = new HashSet<>();
    private boolean firstSnapshot = true;

    /** Setter for test. */
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** Setter for test (allows injecting a stub LiveMetricsService). */
    public void setLiveMetricsService(LiveMetricsService liveMetrics) {
        this.liveMetrics = liveMetrics;
    }

    @FXML
    public void initialize() {
        configureTopBar();
        configurePillar1();
        configurePillar2();
        configurePillar3();
        configurePillar4();
        configureRecommendations();
        configureMapsTab();
        startAutoRefresh();
        startLiveTicker();
    }

    private void configureMapsTab() {
        if (mapsContainer == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/maps.fxml"));
            Parent mapsRoot = loader.load();
            mapsController = loader.getController();
            if (mapsController != null) {
                mapsController.setDashboardService(dashboardService);
                mapsController.setOnZoneSelected(this::onMapZoneSelected);
            }
            mapsContainer.getChildren().setAll(mapsRoot);
        } catch (Exception e) {
            log.warn("Load maps.fxml failed: {}", e.getMessage(), e);
        }
    }

    /** Filters the pillar tables to a single VN region — drill-down from the map. */
    private void onMapZoneSelected(String regionCode) {
        if (regionCode == null) return;
        log.info("VN map drill-down: {}", regionCode);
        // Filter p1/p3/p4 by region (p2 has no region column)
        p1Data.setAll(p1Data.stream()
            .filter(r -> regionCode.equals(r.getRegionCode()))
            .collect(Collectors.toList()));
        p3Data.setAll(p3Data.stream()
            .filter(r -> regionCode.equals(r.getRegionCode()))
            .collect(Collectors.toList()));
        p4Data.setAll(p4Data.stream()
            .filter(r -> regionCode.equals(r.getRegionCode()))
            .collect(Collectors.toList()));
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
        colP1Idr.setCellValueFactory(new PropertyValueFactory<>("idr"));
        colP1Sfri.setCellValueFactory(new PropertyValueFactory<>("sfri"));
        colP1Hhi.setCellValueFactory(new PropertyValueFactory<>("hhiSupply"));
        colP1N1.setCellValueFactory(new PropertyValueFactory<>("n1Resilience"));
        colP1Score.setCellValueFactory(new PropertyValueFactory<>("pillar1Score"));
        colP1Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblPillar1.setItems(p1Data);
    }

    private void configurePillar2() {
        if (tblPillar2 == null) return;
        colP2Fuel.setCellValueFactory(new PropertyValueFactory<>("fuelType"));
        colP2Sigma.setCellValueFactory(new PropertyValueFactory<>("sigma30d"));
        colP2Gap.setCellValueFactory(new PropertyValueFactory<>("priceGapPct"));
        colP2Beta.setCellValueFactory(new PropertyValueFactory<>("betaCrude"));
        colP2Afford.setCellValueFactory(new PropertyValueFactory<>("affordabilityIdx"));
        colP2Score.setCellValueFactory(new PropertyValueFactory<>("pillar2Score"));
        colP2Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblPillar2.setItems(p2Data);
    }

    private void configurePillar3() {
        if (tblPillar3 == null) return;
        colP3Region.setCellValueFactory(new PropertyValueFactory<>("regionCode"));
        colP3Reserve.setCellValueFactory(new PropertyValueFactory<>("reserveMarginPct"));
        colP3Peak.setCellValueFactory(new PropertyValueFactory<>("peakLoadFactor"));
        colP3Shed.setCellValueFactory(new PropertyValueFactory<>("sheddingProb"));
        colP3Freq.setCellValueFactory(new PropertyValueFactory<>("freqStabilityIdx"));
        colP3Score.setCellValueFactory(new PropertyValueFactory<>("pillar3Score"));
        colP3Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblPillar3.setItems(p3Data);
    }

    private void configurePillar4() {
        if (tblPillar4 == null) return;
        colP4Region.setCellValueFactory(new PropertyValueFactory<>("regionCode"));
        colP4Renew.setCellValueFactory(new PropertyValueFactory<>("renewablePct"));
        colP4Co2.setCellValueFactory(new PropertyValueFactory<>("co2Intensity"));
        colP4Curtail.setCellValueFactory(new PropertyValueFactory<>("curtailmentRate"));
        colP4Netzero.setCellValueFactory(new PropertyValueFactory<>("netzeroProgress"));
        colP4Score.setCellValueFactory(new PropertyValueFactory<>("pillar4Score"));
        colP4Status.setCellValueFactory(new PropertyValueFactory<>("status"));
        tblPillar4.setItems(p4Data);
    }

    private void configureRecommendations() {
        if (lstRecommendations == null) return;
        lstRecommendations.setItems(recData);
        lstRecommendations.setCellFactory(list -> new ListCell<Recommendation>() {
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
                    if ("CRITICAL".equals(item.getSeverity())
                            && !knownCriticalAlertIds.contains(item.getId())) {
                        // freshly arrived critical → pulse + fade-in
                        PulseEffect.pulse(this);
                    }
                    FadeTransition fade = new FadeTransition(Duration.millis(700), this);
                    fade.setFromValue(0.0);
                    fade.setToValue(1.0);
                    fade.play();
                }
            }
        });
    }

    private String severityStyle(String severity) {
        if (severity == null) return "";
        switch (severity) {
            case "CRITICAL":
                return "-fx-text-fill: #C62828; -fx-font-weight: 600;";
            case "WARNING":
                return "-fx-text-fill: #EF6C00; -fx-font-weight: 600;";
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

    private void startLiveTicker() {
        if (liveMetrics == null) return;
        liveMetrics.start(sample -> Platform.runLater(() -> applyLiveSample(sample)));
    }

    private void applyLiveSample(LiveMetricsService.Sample sample) {
        if (lblLiveTicker == null || sample == null) return;
        String rate = String.format("%.0f", sample.eventsPerSec);
        lblLiveTicker.setText("⚡ " + rate + " events/sec · Last tick " + sample.tickTime);
        lblLiveTicker.getStyleClass().remove("live-ticker-offline");
        if (!sample.live) {
            lblLiveTicker.getStyleClass().add("live-ticker-offline");
        }
    }

    @FXML
    public void refresh() {
        log.debug("Refresh triggered");
        Task<DashboardSnapshot> task = new Task<DashboardSnapshot>() {
            @Override
            protected DashboardSnapshot call() {
                DashboardSnapshot snap = new DashboardSnapshot();
                snap.score = dashboardService.getSecurityScore().orElse(null);
                snap.p1 = dashboardService.getSupplySecurity();
                snap.p2 = dashboardService.getMarketResilience();
                snap.p3 = dashboardService.getGridReliability();
                snap.p4 = dashboardService.getEnergyTransition();
                snap.recs = dashboardService.getActiveAlerts();
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
        log.debug("applySnapshot: p1={}, p2={}, p3={}, p4={}, recs={}",
            snap.p1 == null ? 0 : snap.p1.size(),
            snap.p2 == null ? 0 : snap.p2.size(),
            snap.p3 == null ? 0 : snap.p3.size(),
            snap.p4 == null ? 0 : snap.p4.size(),
            snap.recs == null ? 0 : snap.recs.size());
        applyScore(snap.score);
        p1Data.setAll(snap.p1 == null ? List.of() : snap.p1);
        p2Data.setAll(snap.p2 == null ? List.of() : snap.p2);
        p3Data.setAll(snap.p3 == null ? List.of() : snap.p3);
        p4Data.setAll(snap.p4 == null ? List.of() : snap.p4);
        // detect freshly-arrived CRITICAL alerts BEFORE we replace recData
        detectNewCriticals(snap.recs);
        recData.setAll(snap.recs == null ? List.of() : snap.recs);
        refreshPillar1Chart(snap.p1);
        refreshPillar2Chart(snap.p2);
        refreshPillar3Chart(snap.p3);
        refreshPillar4Chart(snap.p4);
        pushPillarSparklines(snap);
        if (lblRecCount != null) {
            lblRecCount.setText(recData.size() + " active");
        }
        if (lblLastRefresh != null) {
            lblLastRefresh.setText("Last refresh: " + java.time.LocalTime.now().withNano(0));
        }
        firstSnapshot = false;
    }

    /** Append latest pillar score to each sparkline so 3-min trend is visible. */
    private void pushPillarSparklines(DashboardSnapshot snap) {
        pushAvgScore(sparkP1, snap.p1, Pillar1SupplySecurity::getPillar1Score);
        pushAvgScore(sparkP2, snap.p2, Pillar2MarketResilience::getPillar2Score);
        pushAvgScore(sparkP3, snap.p3, Pillar3GridReliability::getPillar3Score);
        pushAvgScore(sparkP4, snap.p4, Pillar4EnergyTransition::getPillar4Score);
    }

    private <T> void pushAvgScore(Sparkline spark, List<T> rows,
                                  java.util.function.Function<T, BigDecimal> getter) {
        if (spark == null || rows == null || rows.isEmpty()) return;
        double sum = 0;
        int n = 0;
        for (T row : rows) {
            BigDecimal v = getter.apply(row);
            if (v != null) {
                sum += v.doubleValue();
                n++;
            }
        }
        if (n > 0) spark.push(sum / n);
    }

    /** Show toast + remember IDs so we don't re-toast known alerts. */
    private void detectNewCriticals(List<Recommendation> recs) {
        if (recs == null) return;
        Set<Long> currentIds = new HashSet<>();
        for (Recommendation r : recs) {
            currentIds.add(r.getId());
            if ("CRITICAL".equals(r.getSeverity())
                    && !knownCriticalAlertIds.contains(r.getId())
                    && !firstSnapshot
                    && btnLogout != null && btnLogout.getScene() != null) {
                Toast.showCritical(
                        btnLogout.getScene().getWindow(),
                        r.getTitle() == null ? "CRITICAL alert" : r.getTitle(),
                        r.getMessage() == null ? "" : r.getMessage());
            }
        }
        // Trim known set to what's still active so memory doesn't grow unboundedly
        knownCriticalAlertIds.retainAll(currentIds);
        for (Recommendation r : recs) {
            if ("CRITICAL".equals(r.getSeverity())) {
                knownCriticalAlertIds.add(r.getId());
            }
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
            lblScoreStatus.getStyleClass().removeAll(
                    "status-secure", "status-elevated", "status-stressed", "status-critical");
            String css = scoreStatusStyle(score.getStatus());
            if (css != null) lblScoreStatus.getStyleClass().add(css);
        }
        if (scoreGauge != null) {
            scoreGauge.setProgress(score.getOverallScoreRatio());
        }
    }

    /** Maps IEA status string → CSS style class. */
    private String scoreStatusStyle(String status) {
        if (status == null) return null;
        switch (status) {
            case "SECURE":   return "status-secure";
            case "ELEVATED": return "status-elevated";
            case "STRESSED": return "status-stressed";
            case "CRITICAL": return "status-critical";
            default:         return null;
        }
    }

    private void refreshPillar1Chart(List<Pillar1SupplySecurity> rows) {
        if (chartPillar1 == null) return;
        chartPillar1.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("SFRI — days of cover");
        for (Pillar1SupplySecurity r : rows) {
            String label = (r.getRegionCode() == null ? "?" : r.getRegionCode()) + "/" +
                    (r.getFuelType() == null ? "?" : r.getFuelType());
            Number val = r.getSfri() == null ? 0 : r.getSfri();
            series.getData().add(new XYChart.Data<>(label, val));
        }
        chartPillar1.getData().add(series);
    }

    private void refreshPillar2Chart(List<Pillar2MarketResilience> rows) {
        if (chartPillar2 == null) return;
        chartPillar2.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("σ_30d — price volatility");
        for (Pillar2MarketResilience r : rows) {
            String label = r.getFuelType() == null ? "?" : r.getFuelType();
            Number val = r.getSigma30d() == null ? 0 : r.getSigma30d();
            series.getData().add(new XYChart.Data<>(label, val));
        }
        chartPillar2.getData().add(series);
    }

    private void refreshPillar3Chart(List<Pillar3GridReliability> rows) {
        if (chartPillar3 == null) return;
        chartPillar3.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reserve margin %");
        for (Pillar3GridReliability r : rows) {
            String label = r.getRegionCode() == null ? "?" : r.getRegionCode();
            Number val = r.getReserveMarginPct() == null ? 0 : r.getReserveMarginPct();
            series.getData().add(new XYChart.Data<>(label, val));
        }
        chartPillar3.getData().add(series);
        Platform.runLater(() -> colorPillar3Bars(rows));
    }

    /** Colour bars by reserve margin — low margin = red, comfortable = green. */
    private void colorPillar3Bars(List<Pillar3GridReliability> rows) {
        if (chartPillar3.getData().isEmpty()) return;
        XYChart.Series<String, Number> series = chartPillar3.getData().get(0);
        int idx = 0;
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() == null) continue;
            Pillar3GridReliability row = idx < rows.size() ? rows.get(idx) : null;
            String style = "-fx-bar-fill: #2E7D32;";
            if (row != null && row.getReserveMarginPct() != null) {
                double pct = row.getReserveMarginPct().doubleValue();
                if (pct < 5) style = "-fx-bar-fill: #C62828;";
                else if (pct < 10) style = "-fx-bar-fill: #EF6C00;";
                else if (pct < 20) style = "-fx-bar-fill: #F9A825;";
            }
            d.getNode().setStyle(style);
            idx++;
        }
    }

    private void refreshPillar4Chart(List<Pillar4EnergyTransition> rows) {
        if (chartPillar4 == null) return;
        chartPillar4.getData().clear();
        if (rows == null || rows.isEmpty()) return;
        for (Pillar4EnergyTransition r : rows) {
            BigDecimal share = r.getRenewablePct();
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
        } catch (Exception e) {
            log.error("Load FXML {} fail: {}", fxmlPath, e.toString(), e);
            String root = e.getCause() != null ? e.getCause().toString() : e.toString();
            AlertHelper.showError("Lỗi mở màn hình",
                    "Không mở được " + fxmlPath + "\n\nLỗi: " + root);
        }
    }

    /** Called from MainApp on window close to release scheduler + live metrics. */
    public void shutdownScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (liveMetrics != null) {
            liveMetrics.stop();
        }
        if (mapsController != null) {
            mapsController.shutdown();
        }
    }

    /** Snapshot — assembled on background thread, applied atomically on FX thread. */
    private static class DashboardSnapshot {
        SecurityScore score;
        List<Pillar1SupplySecurity> p1;
        List<Pillar2MarketResilience> p2;
        List<Pillar3GridReliability> p3;
        List<Pillar4EnergyTransition> p4;
        List<Recommendation> recs;
    }
}
