package vn.edu.ves.desktop;

/**
 * VES Desktop — concise reference file for teacher demo.
 *
 * Purpose: a single-file summary you can open/print to explain the desktop
 * entry point and its main features without showing the whole codebase.
 *
 * Notes / mapping to real code:
 *  - Entry point: `MainApp` (JavaFX Application) — loads `/fxml/login.fxml`
 *  - Dashboard: `DashboardController` — auto-refresh, pillars, alerts sidebar
 *  - Alerts: `ViewsDao.fetchActiveAlerts()` reads `alerts` (view `v_active_alerts`)
 *  - Recommendations: `v_active_recommendations` view and `recommendations` table
 *  - Maps: `MapsController`, `VietnamMap`, `WorldMapView`
 *  - Alert rules UI: `AlertRuleController` (CRUD), backed by `alert_rules` table
 *  - Run: `mvn -pl desktop-admin -DskipTests javafx:run`
 *
 * This file intentionally contains only a small runnable `main()` that prints
 * the key points — use it as a one-page reference during a demo or viva.
 */
public final class MainReference {

    private MainReference() { }

    public static void main(String[] args) {
        System.out.println("VES Desktop — Reference (Main entry & features)");
        System.out.println();
        System.out.println("Entry point: vn.edu.ves.desktop.MainApp (JavaFX Application)");
        System.out.println();
        System.out.println("Primary screens:");
        System.out.println(" - Login -> /fxml/login.fxml");
        System.out.println(" - Dashboard -> /fxml/dashboard.fxml (4 pillars, alerts sidebar, maps)");
        System.out.println(" - Maps -> /fxml/maps.fxml (Vietnam SVG + world WebView)");
        System.out.println(" - Alert Rules -> /fxml/alertRule.fxml (CRUD for alert_rules)");
        System.out.println(" - Users -> /fxml/user.fxml (ADMIN only)");
        System.out.println();
        System.out.println("Key behaviors and implementation notes:");
        System.out.println(" - Auto-refresh: DashboardController.refresh() runs on a background task every 10s.");
        System.out.println(" - Live ticker: reads Flink JobManager REST metric (numRecordsInPerSecond) via FlinkClient.");
        System.out.println(" - Alerts: Deskop reads view v_active_alerts via ViewsDao.fetchActiveAlerts() and shows them as Recommendation objects.");
        System.out.println(" - Critical alerts: Desktop shows a toast (Toast.showCritical) for freshly-arrived CRITICAL items.");
        System.out.println(" - Maps: MapsController.refresh() updates VietnamMap.applyScores(scores) (colour by pillar score) and WorldMapView.updateMarkers(json).");
        System.out.println();
        System.out.println("Useful file references (in project):");
        System.out.println(" - desktop-admin/src/main/java/vn/edu/ves/desktop/MainApp.java");
        System.out.println(" - desktop-admin/src/main/java/vn/edu/ves/desktop/controller/DashboardController.java");
        System.out.println(" - desktop-admin/src/main/java/vn/edu/ves/desktop/dao/ViewsDao.java");
        System.out.println(" - desktop-admin/src/main/java/vn/edu/ves/desktop/controller/MapsController.java");
        System.out.println(" - desktop-admin/src/main/java/vn/edu/ves/desktop/widget/VietnamMap.java");
        System.out.println(" - infra/script/03_init_alerts.sql (schema: alert_rules + alerts + v_active_alerts)");
        System.out.println();
        System.out.println("How to run locally (demo):");
        System.out.println("  1) Start infra: docker compose -f infra/docker-compose.yml up -d");
        System.out.println("  2) Start generators: ./scripts/start_generators.sh");
        System.out.println("  3) Submit Flink job (if not running): docker cp flink-jobs/target/fuel-flink-job-*.jar flink-jobmanager:/tmp/job.jar && docker exec flink-jobmanager flink run -d /tmp/job.jar");
        System.out.println("  4) Run desktop: mvn -pl desktop-admin -DskipTests javafx:run");
        System.out.println();
        System.out.println("Verification queries (psql):");
        System.out.println("  SELECT * FROM v_active_alerts ORDER BY alert_timestamp DESC LIMIT 10;");
        System.out.println("  SELECT * FROM v_active_recommendations ORDER BY suggested_at DESC LIMIT 10;");
        System.out.println();
        System.out.println("This is a read-only reference for demo purposes.");
    }
}
