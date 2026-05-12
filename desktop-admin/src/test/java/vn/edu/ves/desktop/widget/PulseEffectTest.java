package vn.edu.ves.desktop.widget;

import javafx.animation.Timeline;
import javafx.scene.control.Label;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Verifies {@link PulseEffect} starts and tears itself down cleanly.
 */
public class PulseEffectTest {

    @BeforeClass
    public static void initFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                javafx.application.Platform.startup(latch::countDown);
            } catch (IllegalStateException already) {
                latch.countDown();
            }
        }, "fx-init");
        t.setDaemon(true);
        t.start();
        latch.await(5, TimeUnit.SECONDS);
    }

    @After
    public void cleanup() {
        PulseEffect.stopAll();
    }

    @Test
    public void pulse_attachesDropShadow_andRegistersActive() throws Exception {
        Label[] holder = new Label[1];
        CountDownLatch ready = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            holder[0] = new Label("test");
            ready.countDown();
        });
        ready.await(5, TimeUnit.SECONDS);
        Label n = holder[0];

        CountDownLatch done = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            Timeline tl = PulseEffect.pulse(n);
            assertNotNull(tl);
            done.countDown();
        });
        done.await(5, TimeUnit.SECONDS);
        assertEquals(1, PulseEffect.activeCount());
        assertNotNull(n.getEffect());
    }

    @Test
    public void stopAll_clearsEffectsAndActiveSet() throws Exception {
        Label[] holder = new Label[1];
        CountDownLatch ready = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            holder[0] = new Label("test");
            PulseEffect.pulse(holder[0]);
            ready.countDown();
        });
        ready.await(5, TimeUnit.SECONDS);
        assertEquals(1, PulseEffect.activeCount());

        CountDownLatch done = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            PulseEffect.stopAll();
            done.countDown();
        });
        done.await(5, TimeUnit.SECONDS);
        assertEquals(0, PulseEffect.activeCount());
        assertNull(holder[0].getEffect());
    }
}
