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
        north.setContent("M 120.5 13.5 L 114.4 8.0 L 112.3 10.9 L 102.4 14.1 L 100.2 16.4 L 101.5 21.5 L 96.5 25.6 L 92.6 25.0 L 86.8 30.0 L 83.2 28.3 L 83.0 25.4 L 79.1 26.3 L 77.1 28.4 L 75.3 35.6 L 65.3 26.3 L 62.9 28.8 L 62.9 31.7 L 60.3 33.1 L 55.3 26.4 L 54.2 29.7 L 46.0 38.2 L 42.6 36.4 L 40.8 32.8 L 29.7 27.2 L 26.0 33.1 L 27.9 33.5 L 25.3 34.3 L 23.4 37.8 L 19.8 38.6 L 22.8 44.1 L 34.2 55.0 L 36.1 63.2 L 38.5 62.1 L 40.6 57.2 L 41.4 61.0 L 44.8 60.4 L 45.4 65.4 L 42.1 70.3 L 43.7 70.1 L 42.6 74.0 L 40.0 76.0 L 42.7 76.8 L 49.1 87.5 L 52.1 87.9 L 53.0 86.1 L 53.2 89.9 L 59.7 90.2 L 66.9 95.2 L 70.2 88.8 L 79.0 85.1 L 83.6 87.5 L 84.3 86.6 L 90.8 93.7 L 97.5 97.7 L 101.1 96.2 L 110.2 97.7 L 115.0 101.9 L 122.4 104.6 L 130.4 111.5 L 137.7 114.6 L 137.8 117.5 L 139.5 115.2 L 139.2 117.7 L 147.7 109.7 L 153.1 109.1 L 150.9 107.1 L 153.8 107.8 L 152.8 104.5 L 153.8 102.4 L 151.6 102.6 L 153.5 100.6 L 153.4 99.1 L 151.7 99.2 L 154.7 97.5 L 153.6 95.3 L 155.0 96.4 L 157.0 95.5 L 156.0 94.2 L 160.2 94.8 L 157.0 88.3 L 158.7 86.6 L 154.3 83.7 L 160.2 84.2 L 160.2 86.6 L 163.3 87.5 L 162.0 85.2 L 164.7 86.0 L 166.7 85.5 L 166.1 83.9 L 167.8 84.0 L 167.5 72.6 L 176.9 77.2 L 179.3 74.0 L 184.2 74.1 L 188.6 71.1 L 189.7 67.6 L 193.6 67.7 L 193.6 69.7 L 195.2 69.1 L 189.8 63.4 L 181.7 65.3 L 178.9 63.6 L 175.6 65.0 L 174.6 61.7 L 165.7 57.9 L 166.3 54.5 L 158.2 52.0 L 155.5 52.7 L 155.3 42.8 L 151.2 39.5 L 153.2 33.0 L 156.4 33.2 L 159.8 26.4 L 151.0 22.2 L 143.3 24.2 L 141.8 21.2 L 139.3 20.3 L 131.1 22.3 L 126.6 18.2 L 122.3 17.4 L 120.5 13.5 Z");
        SVGPath central = new SVGPath();
        central.setContent("M 130.1 138.0 L 129.9 129.0 L 137.9 114.8 L 130.0 111.2 L 110.6 97.9 L 101.1 96.6 L 99.2 98.1 L 94.8 96.0 L 87.2 102.0 L 92.1 103.3 L 95.5 101.3 L 96.7 103.3 L 94.5 104.7 L 96.5 106.1 L 95.6 109.1 L 102.9 110.8 L 105.4 113.5 L 104.3 116.8 L 105.5 117.2 L 101.8 118.4 L 100.9 123.1 L 96.8 125.1 L 94.3 129.0 L 90.7 129.1 L 87.4 126.8 L 75.7 127.1 L 78.6 132.8 L 71.7 139.1 L 81.0 143.2 L 97.3 155.0 L 109.5 158.5 L 111.0 160.6 L 113.7 160.4 L 110.1 161.4 L 109.0 166.0 L 113.6 171.8 L 115.6 171.8 L 116.7 174.8 L 120.9 174.2 L 123.4 179.7 L 125.8 180.9 L 124.4 183.8 L 128.8 190.8 L 141.9 203.0 L 145.2 202.8 L 149.3 211.9 L 152.3 211.4 L 152.6 221.5 L 153.5 224.0 L 156.0 224.8 L 156.2 228.9 L 158.3 229.9 L 161.9 226.2 L 165.0 233.5 L 168.7 233.4 L 170.6 234.9 L 170.8 237.8 L 176.2 241.3 L 179.6 240.4 L 180.8 241.2 L 179.1 241.3 L 177.3 245.6 L 171.8 247.5 L 170.5 250.1 L 176.9 258.5 L 179.5 258.4 L 181.1 261.4 L 184.2 261.6 L 183.8 263.7 L 186.0 266.0 L 183.5 272.8 L 179.4 273.5 L 182.6 278.3 L 180.9 280.7 L 182.6 285.5 L 176.3 301.2 L 177.3 305.3 L 180.3 307.3 L 179.2 311.7 L 184.0 320.2 L 183.3 328.0 L 187.0 325.9 L 199.9 324.6 L 207.1 327.2 L 210.3 333.9 L 215.0 336.2 L 216.2 335.1 L 219.0 340.5 L 228.0 343.9 L 223.0 346.7 L 221.6 352.5 L 216.2 352.9 L 216.6 357.7 L 217.9 357.9 L 217.6 360.6 L 219.1 362.1 L 216.7 362.8 L 215.3 372.4 L 216.6 375.3 L 214.4 378.8 L 219.6 385.5 L 225.3 388.1 L 226.8 386.6 L 226.8 378.6 L 230.2 380.3 L 233.5 375.3 L 231.2 373.6 L 232.3 371.3 L 230.0 371.7 L 232.4 367.9 L 231.1 363.6 L 232.5 366.0 L 232.8 370.6 L 234.7 371.2 L 232.0 360.7 L 236.4 361.1 L 237.1 349.5 L 233.5 351.0 L 232.3 347.2 L 237.1 343.0 L 236.7 346.8 L 238.4 346.2 L 239.8 349.3 L 240.2 347.2 L 236.8 340.9 L 240.0 340.8 L 240.2 339.2 L 235.3 331.8 L 235.6 327.1 L 235.0 328.7 L 233.9 327.1 L 235.5 325.8 L 233.3 324.2 L 233.5 321.4 L 235.5 323.7 L 236.2 321.6 L 232.7 317.0 L 235.0 319.5 L 232.8 315.0 L 232.9 311.0 L 233.6 308.9 L 234.9 312.9 L 235.4 309.5 L 228.1 288.2 L 228.8 283.3 L 225.8 279.4 L 223.5 269.8 L 222.2 269.8 L 224.6 266.7 L 220.8 261.0 L 219.3 264.1 L 217.2 259.4 L 215.4 260.4 L 216.6 258.3 L 210.1 251.4 L 208.3 246.8 L 204.2 243.4 L 199.2 243.8 L 204.0 240.0 L 206.0 238.2 L 203.5 238.0 L 197.3 232.3 L 194.5 232.0 L 192.9 234.3 L 190.9 233.6 L 189.9 231.2 L 185.9 225.8 L 179.4 222.8 L 172.7 214.8 L 169.4 209.1 L 159.0 201.2 L 151.4 190.6 L 147.7 189.9 L 149.1 183.9 L 151.6 182.0 L 148.6 176.3 L 143.7 173.5 L 139.2 172.2 L 132.4 164.1 L 128.9 156.3 L 124.3 148.6 L 127.6 145.2 L 130.1 138.0 Z");
        SVGPath south = new SVGPath();
        south.setContent("M 149.0 377.2 L 136.5 374.2 L 134.5 378.1 L 131.7 377.5 L 129.8 379.5 L 131.7 381.7 L 131.3 389.0 L 138.4 395.7 L 140.7 396.2 L 141.5 398.3 L 139.7 399.8 L 141.2 404.9 L 134.1 401.1 L 131.7 402.7 L 127.9 397.2 L 118.6 399.1 L 115.5 402.7 L 108.1 399.5 L 106.2 401.3 L 107.7 406.7 L 101.5 412.2 L 93.0 412.2 L 77.5 416.2 L 75.4 414.8 L 73.4 417.2 L 71.2 417.1 L 77.3 428.0 L 91.3 414.3 L 94.2 423.9 L 99.3 421.6 L 108.6 428.7 L 102.6 432.9 L 100.9 441.9 L 99.2 464.6 L 109.1 469.8 L 115.2 464.7 L 118.6 458.2 L 122.2 454.9 L 141.8 447.5 L 147.5 442.1 L 152.3 441.0 L 153.2 436.4 L 159.9 423.6 L 172.4 414.9 L 174.0 416.8 L 183.6 410.4 L 192.4 406.5 L 196.1 406.9 L 199.3 400.3 L 203.9 399.3 L 205.0 400.5 L 212.1 393.1 L 218.2 392.1 L 219.7 388.7 L 223.1 387.8 L 222.0 385.8 L 219.3 385.3 L 216.8 380.8 L 214.4 381.1 L 216.6 375.4 L 215.3 371.9 L 217.6 361.7 L 216.1 353.5 L 219.0 351.8 L 221.7 352.3 L 223.0 346.7 L 226.8 343.7 L 220.3 341.3 L 215.8 336.4 L 211.1 334.9 L 207.1 327.2 L 198.6 324.4 L 186.1 326.2 L 183.8 327.9 L 180.8 336.2 L 183.5 345.0 L 182.4 356.3 L 178.9 359.5 L 175.4 357.1 L 171.6 358.2 L 166.3 364.2 L 159.8 365.0 L 156.9 368.2 L 148.0 368.0 L 149.0 377.2 Z");

        addZone(mapGroup, VN_NORTH,   "Miền Bắc",  north,   105, 61);
        addZone(mapGroup, VN_CENTRAL, "Miền Trung", central, 171, 237);
        addZone(mapGroup, VN_SOUTH,   "Miền Nam",   south,   160, 395);

        // Sea decoration — small dot offshore (Hoàng Sa / Trường Sa hint)
        // Offshore indicators (Hoàng Sa / Trường Sa) moved slightly for new shape
        Circle paracel = new Circle(230, 200, 3, Color.web("#90A4AE"));
        Circle spratly = new Circle(225, 360, 3, Color.web("#90A4AE"));
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
