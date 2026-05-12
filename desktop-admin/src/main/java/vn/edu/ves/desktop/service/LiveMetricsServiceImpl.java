package vn.edu.ves.desktop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.util.FlinkClient;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Default {@link LiveMetricsService} backed by {@link FlinkClient} +
 * exponential-moving-average smoothing.
 */
public class LiveMetricsServiceImpl implements LiveMetricsService {

    private static final Logger log = LoggerFactory.getLogger(LiveMetricsServiceImpl.class);
    private static final double EMA_ALPHA = 0.30;

    private final DoubleSupplier rawRateSupplier;
    private final long tickIntervalSec;

    private ScheduledExecutorService scheduler;
    private double ema = -1.0;

    /** Production constructor — polls Flink at {@code http://localhost:8081} every 3s. */
    public LiveMetricsServiceImpl() {
        this(new FlinkClient()::fetchAggregateRecordsPerSec, 3L);
    }

    /** Testable constructor — inject any DoubleSupplier (e.g. mock) and tick cadence. */
    public LiveMetricsServiceImpl(DoubleSupplier rawRateSupplier, long tickIntervalSec) {
        this.rawRateSupplier = rawRateSupplier;
        this.tickIntervalSec = tickIntervalSec;
    }

    @Override
    public synchronized void start(Consumer<Sample> onSample) {
        if (scheduler != null) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "live-metrics");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> tick(onSample), 0L, tickIntervalSec, TimeUnit.SECONDS);
        log.info("LiveMetricsService started (interval={}s)", tickIntervalSec);
    }

    @Override
    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
            log.info("LiveMetricsService stopped");
        }
    }

    private void tick(Consumer<Sample> onSample) {
        try {
            double raw = rawRateSupplier.getAsDouble();
            boolean live = raw > 0.0;
            if (ema < 0) {
                ema = raw;
            } else {
                ema = EMA_ALPHA * raw + (1.0 - EMA_ALPHA) * ema;
            }
            Sample s = new Sample(Math.max(0.0, ema), LocalTime.now().withNano(0), live);
            if (onSample != null) onSample.accept(s);
        } catch (Exception e) {
            log.debug("LiveMetricsService tick failed: {}", e.getMessage());
        }
    }
}
