package vn.edu.ves.desktop.service;

import java.time.LocalTime;
import java.util.function.Consumer;

/**
 * Streaming-pipeline live-metrics provider for the top-bar event ticker.
 *
 * <p>Phase 7.2: emits one sample roughly every {@code tickIntervalSec} seconds,
 * containing the smoothed events/sec rate plus the wall-clock tick time. The
 * source is the Flink JobManager REST {@code numRecordsInPerSecond} metric,
 * smoothed via an exponential-moving-average so the ticker doesn't jitter.</p>
 *
 * <p>Designed to be cheap: one HTTP GET per tick, swallowed errors. Callers
 * must call {@link #stop()} on window close to release the scheduler thread.</p>
 */
public interface LiveMetricsService {

    /** Start polling. {@code onSample} is invoked on the scheduler thread — UI consumers must wrap in Platform.runLater. */
    void start(Consumer<Sample> onSample);

    /** Stop polling and release resources. Idempotent. */
    void stop();

    /** Snapshot delivered to subscribers. */
    final class Sample {
        public final double eventsPerSec;
        public final LocalTime tickTime;
        public final boolean live;

        public Sample(double eventsPerSec, LocalTime tickTime, boolean live) {
            this.eventsPerSec = eventsPerSec;
            this.tickTime = tickTime;
            this.live = live;
        }
    }
}
