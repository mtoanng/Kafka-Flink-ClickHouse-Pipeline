package vn.edu.ves.desktop.widget;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Wraps a {@link WebView} hosting the bundled {@code worldmap.html} (Leaflet + OSM tiles).
 *
 * <p>Exposes a single {@link #updateMarkers(String)} bridge for Java → JS calls.  The
 * payload must be a JSON array string of objects of the shape
 * {@code {"code":"WTI_HOUSTON","fuel_type":"WTI_CRUDE","price":78.5,"delta_pct":-0.3}}.</p>
 */
public class WorldMapView extends StackPane {

    private static final Logger log = LoggerFactory.getLogger(WorldMapView.class);

    private final WebView webView;
    private final WebEngine engine;
    private volatile boolean ready = false;
    private String pendingPayload;

    public WorldMapView() {
        webView = new WebView();
        engine = webView.getEngine();
        getChildren().add(webView);

        URL htmlUrl = getClass().getResource("/web/worldmap.html");
        if (htmlUrl == null) {
            log.warn("worldmap.html missing from classpath");
            return;
        }
        engine.load(htmlUrl.toExternalForm());
        engine.getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == Worker.State.SUCCEEDED) {
                ready = true;
                if (pendingPayload != null) {
                    executeUpdate(pendingPayload);
                    pendingPayload = null;
                }
            } else if (newS == Worker.State.FAILED) {
                log.warn("worldmap.html failed to load: {}", engine.getLoadWorker().getException());
            }
        });
    }

    /**
     * Push a fresh payload to the JS layer.  Safe to call before the page
     * has finished loading — the call is queued until ready.
     */
    public void updateMarkers(String jsonArray) {
        if (jsonArray == null) return;
        if (!ready) {
            pendingPayload = jsonArray;
            return;
        }
        executeUpdate(jsonArray);
    }

    private void executeUpdate(String jsonArray) {
        try {
            String escaped = jsonArray
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", " ")
                    .replace("\r", " ");
            engine.executeScript("updateMarkers('" + escaped + "')");
        } catch (Exception e) {
            log.warn("executeScript updateMarkers failed: {}", e.getMessage());
        }
    }

    public WebView getWebView() {
        return webView;
    }
}
