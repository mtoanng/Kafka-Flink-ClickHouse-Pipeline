package vn.edu.ves.desktop.widget;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.WeakHashMap;

/**
 * Helper to attach a brief red-glow drop-shadow pulse to any {@link Node},
 * used when a new CRITICAL alert appears in the recommendation feed.
 *
 * <p>Pure JavaFX {@link Timeline}.  The previous effect on the node is saved
 * and restored when the pulse completes so we don't clobber other styling.</p>
 */
public final class PulseEffect {

    private static final int CYCLES = 4;
    private static final double PEAK_RADIUS = 24.0;
    private static final WeakHashMap<Node, Timeline> ACTIVE = new WeakHashMap<>();

    private PulseEffect() {
    }

    /** Attach a red pulsing drop-shadow to {@code node} for ~2 seconds. */
    public static Timeline pulse(Node node) {
        return pulse(node, Color.web("#C62828"));
    }

    /** Pulse using a custom colour. */
    public static Timeline pulse(Node node, Color color) {
        if (node == null) return null;
        Timeline existing = ACTIVE.get(node);
        if (existing != null) {
            existing.stop();
        }
        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(0.0);
        shadow.setSpread(0.20);
        node.setEffect(shadow);

        Timeline tl = new Timeline();
        tl.setCycleCount(CYCLES);
        tl.setAutoReverse(true);
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(250),
                new KeyValue(shadow.radiusProperty(), PEAK_RADIUS)));
        tl.setOnFinished(ev -> {
            node.setEffect(null);
            ACTIVE.remove(node);
        });
        ACTIVE.put(node, tl);
        tl.play();
        return tl;
    }

    /** Test helper — how many pulses are currently active. */
    public static int activeCount() {
        return ACTIVE.size();
    }

    /** Test helper — stop all active pulses immediately (clears drop-shadow effect). */
    public static void stopAll() {
        for (var entry : ACTIVE.entrySet()) {
            entry.getValue().stop();
            if (entry.getKey() != null) entry.getKey().setEffect(null);
        }
        ACTIVE.clear();
    }
}
