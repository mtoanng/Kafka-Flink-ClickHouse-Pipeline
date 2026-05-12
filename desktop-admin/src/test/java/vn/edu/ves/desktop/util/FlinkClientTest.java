package vn.edu.ves.desktop.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link FlinkClient} against an embedded {@link HttpServer} that
 * mimics a tiny slice of the Flink JobManager REST API.
 */
public class FlinkClientTest {

    private HttpServer server;
    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/jobs/overview", this::handleOverview);
        server.createContext("/jobs/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/metrics", this::handleMetricsA);
        server.createContext("/jobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb/metrics", this::handleMetricsB);
        server.setExecutor(null);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @After
    public void tearDown() {
        if (server != null) server.stop(0);
    }

    @Test
    public void fetchRunningJobIds_filtersByStatus() {
        FlinkClient c = new FlinkClient(baseUrl);
        List<String> ids = c.fetchRunningJobIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertTrue(ids.contains("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));
    }

    @Test
    public void fetchJobMetric_parsesNumericValue() {
        FlinkClient c = new FlinkClient(baseUrl);
        double v = c.fetchJobMetric("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "numRecordsInPerSecond");
        assertEquals(125.0, v, 0.0001);
    }

    @Test
    public void fetchAggregateRecordsPerSec_sumsAcrossJobs() {
        FlinkClient c = new FlinkClient(baseUrl);
        double total = c.fetchAggregateRecordsPerSec();
        assertEquals(125.0 + 320.0, total, 0.0001);
    }

    @Test
    public void fetchAggregateRecordsPerSec_unreachableHost_returnsZero() {
        // No process listens on this port; FlinkClient should swallow + return 0.
        FlinkClient c = new FlinkClient("http://127.0.0.1:1");
        assertEquals(0.0, c.fetchAggregateRecordsPerSec(), 0.0001);
    }

    /* ----- handlers ----- */

    private void handleOverview(HttpExchange ex) throws IOException {
        String body = "{\"jobs\":["
                + "{\"id\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\",\"status\":\"RUNNING\"},"
                + "{\"id\":\"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\",\"status\":\"RUNNING\"},"
                + "{\"id\":\"cccccccccccccccccccccccccccccccc\",\"status\":\"FINISHED\"}"
                + "]}";
        respond(ex, body);
    }

    private void handleMetricsA(HttpExchange ex) throws IOException {
        respond(ex, "[{\"id\":\"numRecordsInPerSecond\",\"value\":\"125.0\"}]");
    }

    private void handleMetricsB(HttpExchange ex) throws IOException {
        respond(ex, "[{\"id\":\"numRecordsInPerSecond\",\"value\":\"320.0\"}]");
    }

    private void respond(HttpExchange ex, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}
