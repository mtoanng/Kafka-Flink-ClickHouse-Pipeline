package vn.edu.ves.desktop.widget;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tiny inline trend chart for the pillar tab headers.
 *
 * <p>Pure JavaFX {@link Canvas} — no extra dependencies.  Stores up to
 * {@code maxPoints} samples in a FIFO {@link Deque} and redraws on each
 * {@link #push(double)} call.  Negative or NaN samples are coerced to 0.</p>
 *
 * <p>Designed for a 60 × 20 px footprint but accepts arbitrary dimensions.</p>
 */
public class Sparkline extends Canvas {

    public static final int DEFAULT_MAX_POINTS = 60;
    private static final double LINE_WIDTH = 1.4;

    private final Deque<Double> samples = new ArrayDeque<>();
    private final int maxPoints;
    private Color strokeColor = Color.web("#1976D2");
    private Color fillColor = Color.web("#1976D2", 0.18);

    public Sparkline() {
        this(60.0, 20.0, DEFAULT_MAX_POINTS);
    }

    public Sparkline(double width, double height, int maxPoints) {
        super(width, height);
        this.maxPoints = Math.max(2, maxPoints);
        draw();
    }

    /** Append one sample; oldest is evicted once {@code maxPoints} reached. */
    public synchronized void push(double value) {
        double v = (Double.isNaN(value) || Double.isInfinite(value) || value < 0) ? 0 : value;
        if (samples.size() >= maxPoints) {
            samples.pollFirst();
        }
        samples.addLast(v);
        draw();
    }

    /** Reset to empty (used when switching contexts). */
    public synchronized void clear() {
        samples.clear();
        draw();
    }

    /** Visible for testing — current sample window size. */
    public synchronized int sampleCount() {
        return samples.size();
    }

    /** Visible for testing — max retained samples. */
    public int capacity() {
        return maxPoints;
    }

    public void setStrokeColor(Color color) {
        this.strokeColor = color == null ? Color.web("#1976D2") : color;
        this.fillColor = strokeColor.deriveColor(0, 1, 1, 0.18);
        draw();
    }

    private void draw() {
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        g.clearRect(0, 0, w, h);
        if (samples.size() < 2) return;

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double v : samples) {
            if (v < min) min = v;
            if (v > max) max = v;
        }
        double range = Math.max(1e-6, max - min);
        double step = w / (maxPoints - 1.0);
        int n = samples.size();
        int offset = maxPoints - n;

        // Fill area beneath the curve
        g.setFill(fillColor);
        g.beginPath();
        int i = 0;
        for (double v : samples) {
            double x = (offset + i) * step;
            double y = h - ((v - min) / range) * (h - 2) - 1;
            if (i == 0) g.moveTo(x, h);
            g.lineTo(x, y);
            i++;
        }
        g.lineTo((offset + n - 1) * step, h);
        g.closePath();
        g.fill();

        // Stroke the curve
        g.setStroke(strokeColor);
        g.setLineWidth(LINE_WIDTH);
        g.beginPath();
        i = 0;
        for (double v : samples) {
            double x = (offset + i) * step;
            double y = h - ((v - min) / range) * (h - 2) - 1;
            if (i == 0) g.moveTo(x, y);
            else g.lineTo(x, y);
            i++;
        }
        g.stroke();
    }
}
