package org.fuel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fuel.model.FuelPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lấy dữ liệu giá dầu thực từ Alpha Vantage API (miễn phí).
 *
 * ══════════════════════════════════════════════════════
 * ĐĂNG KÝ API KEY (miễn phí):
 *   https://www.alphavantage.co/support/#api-key
 *   → Nhập email → nhận API key ngay lập tức
 *   → Free tier: 25 requests/ngày (đủ cho demo)
 *
 * ENDPOINTS được dùng:
 *   WTI Crude Oil: function=WTI&interval=daily
 *   Brent Crude  : function=BRENT&interval=daily
 *   Natural Gas  : function=NATURAL_GAS&interval=daily
 *
 * Response JSON mẫu (WTI):
 * {
 *   "name": "West Texas Intermediate (WTI) Crude Oil Prices",
 *   "data": [
 *     { "date": "2025-04-11", "value": "78.45" },
 *     { "date": "2025-04-10", "value": "77.92" },
 *     ...
 *   ]
 * }
 * ══════════════════════════════════════════════════════
 *
 * NGƯỜI 1 cần làm:
 *   1. Sửa API_KEY bên dưới thành key của bạn
 *   2. Chạy thử: java HttpApiSource.main()
 *   3. Trong FuelPriceProducer.java, đổi:
 *        MockDataGenerator generator = new MockDataGenerator();
 *      thành:
 *        HttpApiSource generator = new HttpApiSource();
 *   Không cần sửa gì khác.
 */
public class HttpApiSource {

    private static final Logger log = LoggerFactory.getLogger(HttpApiSource.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ════════════════════════════════════════════════════════════════
    // ⚠️  THAY API_KEY = "demo" BẰNG KEY CỦA BẠN TỪ alphavantage.co
    // ════════════════════════════════════════════════════════════════
    private static final String API_KEY = "demo";

    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Lấy batch giá mới nhất từ Alpha Vantage.
     * Mỗi lần gọi trả về 3 records (WTI, Brent, Natural Gas).
     *
     * Lưu ý: API trả về daily data nên giá sẽ thay đổi 1 lần/ngày.
     * Để simulate realtime, MockDataGenerator vẫn được dùng sau khi
     * lấy được base price từ API.
     */
    public List<FuelPrice> nextBatch() {
        List<FuelPrice> batch = new ArrayList<>();
        String now = LocalDateTime.now().format(FMT);

        // WTI Crude Oil
        fetchCommodity("WTI",         "WTI_CRUDE",   "USD/barrel", "New York",  "North America", now, batch);
        // Brent Crude Oil
        fetchCommodity("BRENT",       "BRENT_CRUDE", "USD/barrel", "London",    "Europe",        now, batch);
        // Natural Gas (Henry Hub)
        fetchCommodity("NATURAL_GAS", "NATURAL_GAS", "USD/MMBtu",  "Houston",   "North America", now, batch);

        return batch;
    }

    private void fetchCommodity(String function, String fuelType, String priceUnit,
                                String location, String region, String timestamp,
                                List<FuelPrice> batch) {
        String url = String.format("%s?function=%s&interval=daily&apikey=%s",
                BASE_URL, function, API_KEY);
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                log.warn("API {} trả về HTTP {}", function, resp.statusCode());
                return;
            }

            JsonNode root = MAPPER.readTree(resp.body());

            // Kiểm tra rate limit
            if (root.has("Note") || root.has("Information")) {
                log.warn("Alpha Vantage rate limit: {}", root.has("Note") ?
                        root.get("Note").asText() : root.get("Information").asText());
                return;
            }

            // Lấy record mới nhất (phần tử đầu tiên của mảng "data")
            JsonNode dataArray = root.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.size() == 0) {
                log.warn("API {} không có data", function);
                return;
            }

            JsonNode latest = dataArray.get(0);
            String date  = latest.get("date").asText();
            double price = Double.parseDouble(latest.get("value").asText());

            batch.add(FuelPrice.builder()
                    .timestamp(timestamp)
                    .fuelType(fuelType)
                    .price(price)
                    .priceUnit(priceUnit)
                    .location(location)
                    .region(region)
                    .source("alpha-vantage")
                    .build());

            log.info("Alpha Vantage [{}]: {} {} (date: {})", fuelType, price, priceUnit, date);

        } catch (Exception e) {
            log.error("Lỗi khi gọi Alpha Vantage [{}]: {}", function, e.getMessage());
        }
    }

    // ── Test standalone ────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        System.out.println("=== Test Alpha Vantage API ===");
        HttpApiSource source = new HttpApiSource();
        List<FuelPrice> batch = source.nextBatch();
        if (batch.isEmpty()) {
            System.out.println("Không lấy được dữ liệu. Kiểm tra API_KEY và kết nối mạng.");
        } else {
            System.out.printf("Lấy được %d records:%n", batch.size());
            batch.forEach(fp -> System.out.printf(
                    "  %s @ %s: %.2f %s%n", fp.getFuelType(), fp.getLocation(), fp.getPrice(), fp.getPriceUnit()));
        }
    }
}
