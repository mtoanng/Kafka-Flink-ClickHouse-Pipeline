package vn.edu.ves.desktop.widget;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Vietnam-shaped clickable SVG widget.  Three approximated polygon zones —
 * VN_NORTH / VN_CENTRAL / VN_SOUTH — each colorable by Grid Reliability
 * (Pillar 3) score and emitting a region-code event on click.
 *
 * <p>Cartographic accuracy is intentionally low (demo / education).  The
 * three shapes give a recognisable Vietnam silhouette while staying simple
 * enough to maintain.</p>
 */
public class VietnamMap extends StackPane {

    public static final String VN_NORTH = "VN_NORTH";
    public static final String VN_CENTRAL = "VN_CENTRAL";
    public static final String VN_SOUTH = "VN_SOUTH";

    private final Map<String, SVGPath> zones = new LinkedHashMap<>();
    private final Map<String, Label> labels = new LinkedHashMap<>();
    private final StringProperty selectedZone = new SimpleStringProperty(null);
    private Consumer<String> onZoneClick;

    public VietnamMap() {
        setPrefSize(280, 520);
        setPadding(new Insets(8));

        Pane canvas = new Pane();
        canvas.setPrefSize(260, 480);
        canvas.setStyle("-fx-background-color: #F5F7FA; -fx-background-radius: 8;");

        Group mapGroup = new Group();
        // Approximate VN outline split into 3 zones.  Coordinates are in the
        // canvas's local 260x480 space.  Crude but recognisable.
        SVGPath north = new SVGPath();
        north.setContent("M 130 20 L 180 30 L 220 60 L 230 110 L 200 150 L 150 170 L 110 160 L 80 130 L 70 80 L 100 40 Z");
        SVGPath central = new SVGPath();
        central.setContent("M 110 160 L 200 150 L 210 200 L 200 250 L 180 300 L 150 320 L 120 310 L 110 270 L 100 220 L 105 180 Z");
        SVGPath south = new SVGPath();
        south.setContent("M 120 310 L 180 300 L 200 340 L 180 400 L 140 440 L 100 430 L 80 390 L 90 350 Z");

        addZone(mapGroup, VN_NORTH, "Miền Bắc", north, 145, 95);
        addZone(mapGroup, VN_CENTRAL, "Miền Trung", central, 145, 230);
        addZone(mapGroup, VN_SOUTH, "Miền Nam", south, 130, 380);

        // Sea decoration — small dot offshore (Hoàng Sa / Trường Sa hint)
        Circle paracel = new Circle(225, 220, 3, Color.web("#90A4AE"));
        Circle spratly = new Circle(215, 360, 3, Color.web("#90A4AE"));
        mapGroup.getChildren().addAll(paracel, spratly);

        canvas.getChildren().add(mapGroup);

        VBox legend = buildLegend();

        VBox root = new VBox(10, canvas, legend);
        root.setAlignment(Pos.TOP_CENTER);
        getChildren().add(root);
    }

    private void addZone(Group g, String code, String labelText, SVGPath path, double labelX, double labelY) {
        path.setFill(Color.web("#90CAF9"));
        path.setStroke(Color.web("#0D47A1"));
        path.setStrokeWidth(1.2);
        path.setOnMouseEntered(e -> path.setStrokeWidth(2.6));
        path.setOnMouseExited(e -> path.setStrokeWidth(1.2));
        path.setOnMouseClicked(e -> {
            selectedZone.set(code);
            if (onZoneClick != null) onZoneClick.accept(code);
        });
        Text label = new Text(labelText);
        label.setX(labelX);
        label.setY(labelY);
        label.setFill(Color.web("#1A237E"));
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 600;");

        g.getChildren().addAll(path, label);
        zones.put(code, path);

        // Off-canvas live status label (rendered into legend)
        Label statusLbl = new Label(code + ": --");
        statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #455a64;");
        labels.put(code, statusLbl);
    }

    private VBox buildLegend() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 6;");
        Label title = new Label("Grid Reliability — Status");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #263238;");
        box.getChildren().add(title);
        for (Label l : labels.values()) {
            box.getChildren().add(l);
        }
        HBox swatches = new HBox(8,
                swatch("#2E7D32", "SECURE"),
                swatch("#F9A825", "ELEVATED"),
                swatch("#EF6C00", "STRESSED"),
                swatch("#C62828", "CRITICAL"));
        swatches.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(swatches);
        return box;
    }

    private HBox swatch(String color, String text) {
        Circle dot = new Circle(5, Color.web(color));
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 10px;");
        HBox row = new HBox(4, dot, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Repaint each zone from its current Pillar 3 score.
     * Null score → neutral grey.
     */
    public void applyScores(Map<String, BigDecimal> scoresByRegion) {
        if (scoresByRegion == null) return;
        for (Map.Entry<String, SVGPath> entry : zones.entrySet()) {
            BigDecimal s = scoresByRegion.get(entry.getKey());
            entry.getValue().setFill(colorForScore(s));
            Label statusLbl = labels.get(entry.getKey());
            if (statusLbl != null) {
                statusLbl.setText(entry.getKey() + ": " +
                        (s == null ? "--" : s.toPlainString() + " (" + statusForScore(s) + ")"));
            }
        }
    }

    /** Pure mapping function from numeric score → CSS hex colour (used by tests too). */
    public static Color colorForScore(BigDecimal score) {
        if (score == null) return Color.web("#B0BEC5");
        double v = score.doubleValue();
        if (v >= 80) return Color.web("#2E7D32"); // SECURE
        if (v >= 60) return Color.web("#F9A825"); // ELEVATED
        if (v >= 40) return Color.web("#EF6C00"); // STRESSED
        return Color.web("#C62828");              // CRITICAL
    }

    /** Pure mapping function from score → IEA status label. */
    public static String statusForScore(BigDecimal score) {
        if (score == null) return "NO_DATA";
        double v = score.doubleValue();
        if (v >= 80) return "SECURE";
        if (v >= 60) return "ELEVATED";
        if (v >= 40) return "STRESSED";
        return "CRITICAL";
    }

    /** Read-only property of last-clicked zone code. */
    public StringProperty selectedZoneProperty() {
        return selectedZone;
    }

    /** Click handler — receives the region code (VN_NORTH / VN_CENTRAL / VN_SOUTH). */
    public void setOnZoneClick(Consumer<String> handler) {
        this.onZoneClick = handler;
    }
}
