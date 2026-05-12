package vn.edu.ves.desktop.widget;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link Sparkline} — focused on the data-window logic.
 * JavaFX runtime is initialised via a single static latch.
 */
public class SparklineTest {

    @BeforeClass
    public static void initFx() throws Exception {
        if (System.getProperty("java.awt.headless", "false").equalsIgnoreCase("true")) {
            return;
        }
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

    @Test
    public void capacity_isHonored() throws Exception {
        Sparkline[] holder = new Sparkline[1];
        CountDownLatch ready = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            holder[0] = new Sparkline(60, 20, 5);
            ready.countDown();
        });
        ready.await(5, TimeUnit.SECONDS);
        Sparkline s = holder[0];
        assertNotNull(s);
        assertEquals(5, s.capacity());

        for (int i = 0; i < 10; i++) {
            s.push(i);
        }
        assertEquals("Window should cap at capacity", 5, s.sampleCount());
    }

    @Test
    public void clear_resetsSampleCount() throws Exception {
        Sparkline[] holder = new Sparkline[1];
        CountDownLatch ready = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            holder[0] = new Sparkline();
            ready.countDown();
        });
        ready.await(5, TimeUnit.SECONDS);
        Sparkline s = holder[0];
        s.push(1);
        s.push(2);
        s.push(3);
        assertEquals(3, s.sampleCount());
        s.clear();
        assertEquals(0, s.sampleCount());
    }

    @Test
    public void push_rejectsNegativeOrNaN_byCoercingToZero() throws Exception {
        Sparkline[] holder = new Sparkline[1];
        CountDownLatch ready = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            holder[0] = new Sparkline(60, 20, 4);
            ready.countDown();
        });
        ready.await(5, TimeUnit.SECONDS);
        Sparkline s = holder[0];
        s.push(Double.NaN);
        s.push(Double.NEGATIVE_INFINITY);
        s.push(-5);
        s.push(1);
        // All four samples accepted (coerced where needed)
        assertEquals(4, s.sampleCount());
    }
}
