package vn.edu.ves.desktop.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.model.Pillar2MarketResilience;
import vn.edu.ves.desktop.model.Pillar3GridReliability;
import vn.edu.ves.desktop.service.DashboardService;
import vn.edu.ves.desktop.service.DashboardServiceImpl;
import vn.edu.ves.desktop.widget.VietnamMap;
import vn.edu.ves.desktop.widget.WorldMapView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Drives the "Maps" tab: Vietnam SVG zones on the left, Leaflet world map on the right.
 *
 * <p>Both maps refresh from the same {@link DashboardService} every 10 seconds.
 * Vietnam zones are coloured by Pillar 3 (Grid Reliability) score; world markers
 * are tinted by Pillar 2 (Market Resilience) price gap.</p>
 */
public class MapsController {

    private static final Logger log = LoggerFactory.getLogger(MapsController.class);
    private static final long REFRESH_INTERVAL_SEC = 10L;

    @FXML private StackPane vnMapContainer;
    @FXML private StackPane worldMapContainer;
    @FXML private Label lblDrillDown;
    @FXML private VBox vnInfoBox;

    private DashboardService dashboardService = new DashboardServiceImpl();
    private VietnamMap vietnamMap;
    private WorldMapView worldMap;
    private ScheduledExecutorService scheduler;
    private Consumer<String> onZoneSelected;

    public void setDashboardService(DashboardService svc) {
        this.dashboardService = svc;
    }

    /** Called by the parent dashboard so a VN-zone click can filter pillar tables. */
    public void setOnZoneSelected(Consumer<String> handler) {
        this.onZoneSelected = handler;
        if (vietnamMap != null) {
            vietnamMap.setOnZoneClick(code -> {
                if (lblDrillDown != null) lblDrillDown.setText("Drill-down: " + code);
                if (onZoneSelected != null) onZoneSelected.accept(code);
            });
        }
    }

    @FXML
    public void initialize() {
        try {
            vietnamMap = new VietnamMap();
            if (vnMapContainer != null) vnMapContainer.getChildren().add(vietnamMap);
            vietnamMap.setOnZoneClick(code -> {
                if (lblDrillDown != null) lblDrillDown.setText("Drill-down: " + code);
                if (onZoneSelected != null) onZoneSelected.accept(code);
            });
        } catch (Exception e) {
            log.warn("VietnamMap init failed: {}", e.getMessage(), e);
        }
        try {
            worldMap = new WorldMapView();
            if (worldMapContainer != null) worldMapContainer.getChildren().add(worldMap);
        } catch (Exception e) {
            log.warn("WorldMapView init failed (no JavaFX-web?): {}", e.getMessage(), e);
        }
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refresh();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "maps-refresh");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::refresh,
                REFRESH_INTERVAL_SEC, REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    @FXML
    public void refresh() {
        Thread t = new Thread(() -> {
            try {
                List<Pillar3GridReliability> p3 = dashboardService.getGridReliability();
                List<Pillar2MarketResilience> p2 = dashboardService.getMarketResilience();
                Map<String, BigDecimal> scores = new HashMap<>();
                for (Pillar3GridReliability row : p3) {
                    scores.put(row.getRegionCode(), row.getPillar3Score());
                }
                String markersJson = buildMarkersJson(p2);
                Platform.runLater(() -> {
                    if (vietnamMap != null) vietnamMap.applyScores(scores);
                    if (worldMap != null) worldMap.updateMarkers(markersJson);
                });
            } catch (Exception e) {
                log.debug("MapsController refresh failed: {}", e.getMessage());
            }
        }, "maps-refresh-task");
        t.setDaemon(true);
        t.start();
    }

    /** Build the JSON payload sent to worldmap.html#updateMarkers. */
    static String buildMarkersJson(List<Pillar2MarketResilience> p2) {
        // We only have aggregate Pillar 2 metrics per fuel type, not per geographic hub.
        // Map fuel_type → (code, hub). Output one row per hub from the same fuel where helpful.
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Pillar2MarketResilience r : p2) {
            if (r == null || r.getFuelType() == null) continue;
            String fuel = r.getFuelType();
            String code;
            switch (fuel) {
                case "WTI_CRUDE":   code = "WTI_HOUSTON"; break;
                case "BRENT_CRUDE": code = "BRENT_LDN";   break;
                case "GASOLINE":    code = "VN_HAIPHONG"; break;
                case "DIESEL":      code = "VN_VUNGTAU";  break;
                case "NATURAL_GAS": code = "ASIA_SGP";    break;
                default:            code = "ASIA_SGP";    break;
            }
            BigDecimal afford = r.getAffordabilityIdx();
            BigDecimal gap = r.getPriceGapPct();
            if (!first) sb.append(',');
            sb.append('{')
              .append("\"code\":\"").append(code).append("\",")
              .append("\"fuel_type\":\"").append(fuel).append("\",")
              .append("\"price\":").append(afford == null ? "null" : afford.toPlainString()).append(',')
              .append("\"delta_pct\":").append(gap == null ? "0" : gap.toPlainString())
              .append('}');
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}
