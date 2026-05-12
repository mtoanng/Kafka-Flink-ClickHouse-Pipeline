package vn.edu.ves.desktop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight wrapper around the Flink JobManager REST API.
 *
 * <p>Phase 7.2: parses the small amount of JSON we need (job IDs, metric values)
 * with regex so we don't have to add a JSON dependency to the desktop module.
 * If parsing fails or the cluster is unreachable, methods return safe defaults
 * (empty list / 0.0). Callers must tolerate these.</p>
 *
 * <p>Public surface is intentionally tiny:</p>
 * <ul>
 *   <li>{@link #fetchRunningJobIds()}</li>
 *   <li>{@link #fetchJobMetric(String, String)}</li>
 *   <li>{@link #fetchAggregateRecordsPerSec()}</li>
 * </ul>
 */
public class FlinkClient {

    private static final Logger log = LoggerFactory.getLogger(FlinkClient.class);

    /** Default JobManager URL — Flink REST exposed on port 8081 inside docker compose. */
    public static final String DEFAULT_BASE_URL = "http://localhost:8081";
    private static final int CONNECT_TIMEOUT_MS = 1500;
    private static final int READ_TIMEOUT_MS = 2500;

    private static final Pattern JOB_ID_PATTERN =
            Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-f]{32})\"");
    private static final Pattern JOB_STATE_RUNNING_PATTERN =
            Pattern.compile("\"status\"\\s*:\\s*\"RUNNING\"");
    private static final Pattern METRIC_VALUE_PATTERN =
            Pattern.compile("\"value\"\\s*:\\s*\"([\\-0-9.eE]+)\"");

    private final String baseUrl;

    public FlinkClient() {
        this(DEFAULT_BASE_URL);
    }

    public FlinkClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? DEFAULT_BASE_URL : baseUrl.replaceAll("/+$", "");
    }

    /** All job IDs currently in RUNNING state. Empty list if cluster offline. */
    public List<String> fetchRunningJobIds() {
        String body = httpGet(baseUrl + "/jobs/overview");
        List<String> ids = new ArrayList<>();
        if (body == null || body.isEmpty()) return ids;
        // Naive parse: walk JSON chunks separated by '},{' — robust enough for the
        // 1-2 jobs we typically have. Each chunk contains both id and state.
        for (String chunk : body.split("\\},\\s*\\{")) {
            Matcher idM = JOB_ID_PATTERN.matcher(chunk);
            if (idM.find() && JOB_STATE_RUNNING_PATTERN.matcher(chunk).find()) {
                ids.add(idM.group(1));
            }
        }
        return ids;
    }

    /**
     * Fetch a single named metric for the given job (sum across all vertices).
     * Returns 0.0 if the metric is absent or the call fails.
     */
    public double fetchJobMetric(String jobId, String metricName) {
        if (jobId == null || metricName == null) return 0.0;
        String url = baseUrl + "/jobs/" + jobId + "/metrics?get=" + metricName;
        String body = httpGet(url);
        if (body == null || body.isEmpty()) return 0.0;
        double sum = 0.0;
        Matcher m = METRIC_VALUE_PATTERN.matcher(body);
        while (m.find()) {
            try {
                sum += Double.parseDouble(m.group(1));
            } catch (NumberFormatException ignore) {
                /* leave sum unchanged */
            }
        }
        return sum;
    }

    /**
     * Convenience: aggregate numRecordsInPerSecond across all running jobs.
     * Returns 0.0 if Flink is unreachable — UI should fall back to a DB proxy.
     */
    public double fetchAggregateRecordsPerSec() {
        double total = 0.0;
        for (String jobId : fetchRunningJobIds()) {
            total += fetchJobMetric(jobId, "numRecordsInPerSecond");
        }
        return total;
    }

    /** GET with short timeouts. Returns body string or null on failure. */
    String httpGet(String url) {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                log.debug("Flink GET {} returned HTTP {}", url, code);
                return null;
            }
            try (InputStream is = conn.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.debug("Flink GET {} failed: {}", url, e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
