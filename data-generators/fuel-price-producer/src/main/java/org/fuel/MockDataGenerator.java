package org.fuel;

import org.fuel.model.FuelPrice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Sinh dữ liệu giả lập (mock) giá dầu thô / nhiên liệu THẾ GIỚI.
 *
 * Cách hoạt động:
 *  - Mỗi loại dầu × mỗi sàn giao dịch có một giá cơ sở (base price).
 *  - Mỗi lần gọi nextBatch(), giá thay đổi ngẫu nhiên ± 0.3% (random walk).
 *  - Giá bị giới hạn trong khoảng [basePrice×0.80, basePrice×1.20].
 *
 * Loại nhiên liệu & đơn vị:
 *  WTI_CRUDE        → USD/barrel   (NYMEX, New York)
 *  BRENT_CRUDE      → USD/barrel   (ICE,   London)
 *  GASOLINE_FUTURES → USD/gallon   (NYMEX, New York)
 *  DIESEL           → USD/gallon   (US market)
 *  NATURAL_GAS      → USD/MMBtu    (Henry Hub, Houston)
 *
 * Để dùng API thực (Alpha Vantage), thay class này bằng HttpApiSource.java —
 * không cần đụng đến FuelPriceProducer.java.
 */
public class MockDataGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ── Giá cơ sở thực tế (tháng 4/2025) ───────────────────────────────────
    private static final Map<String, Double> BASE_PRICES = new LinkedHashMap<>();
    static {
        BASE_PRICES.put("WTI_CRUDE",        75.80);   // USD/barrel
        BASE_PRICES.put("BRENT_CRUDE",      79.50);   // USD/barrel
        BASE_PRICES.put("GASOLINE_FUTURES",  2.34);   // USD/gallon
        BASE_PRICES.put("DIESEL",            2.68);   // USD/gallon
        BASE_PRICES.put("NATURAL_GAS",       2.10);   // USD/MMBtu
    }

    // ── Đơn vị giá tương ứng ────────────────────────────────────────────────
    private static final Map<String, String> PRICE_UNITS = new LinkedHashMap<>();
    static {
        PRICE_UNITS.put("WTI_CRUDE",        "USD/barrel");
        PRICE_UNITS.put("BRENT_CRUDE",      "USD/barrel");
        PRICE_UNITS.put("GASOLINE_FUTURES", "USD/gallon");
        PRICE_UNITS.put("DIESEL",           "USD/gallon");
        PRICE_UNITS.put("NATURAL_GAS",      "USD/MMBtu");
    }

    // ── Sàn giao dịch / thị trường & vùng ──────────────────────────────────
    private static final Map<String, String> LOCATIONS = new LinkedHashMap<>();
    static {
        LOCATIONS.put("New York (NYMEX)", "North America");
        LOCATIONS.put("London (ICE)",     "Europe");
        LOCATIONS.put("Singapore (SGX)",  "Asia Pacific");
        LOCATIONS.put("Houston",          "North America");
        LOCATIONS.put("Rotterdam",        "Europe");
        LOCATIONS.put("Tokyo",            "Asia Pacific");
    }

    // ── Trạng thái giá hiện tại (random walk) ───────────────────────────────
    // key = "FUEL_TYPE:LOCATION"  →  giá hiện tại
    private final Map<String, Double> currentPrices = new HashMap<>();
    private final Random random = new Random();

    public MockDataGenerator() {
        // Khởi tạo: mỗi sàn có độ chênh ±1% so với giá cơ sở
        for (Map.Entry<String, Double> fuel : BASE_PRICES.entrySet()) {
            for (String location : LOCATIONS.keySet()) {
                String key = fuel.getKey() + ":" + location;
                double locationBias = 1.0 + (random.nextDouble() - 0.5) * 0.02;
                currentPrices.put(key, fuel.getValue() * locationBias);
            }
        }
    }

    /**
     * Trả về batch giá mới cho mỗi cặp (fuel_type × location).
     * Mỗi batch = 5 loại dầu × 6 sàn = 30 records.
     */
    public List<FuelPrice> nextBatch() {
        List<FuelPrice> batch = new ArrayList<>();
        String now = LocalDateTime.now().format(FORMATTER);

        for (Map.Entry<String, Double> fuel : BASE_PRICES.entrySet()) {
            String fuelType  = fuel.getKey();
            double basePrice = fuel.getValue();
            String priceUnit = PRICE_UNITS.get(fuelType);

            for (Map.Entry<String, String> loc : LOCATIONS.entrySet()) {
                String location = loc.getKey();
                String region   = loc.getValue();
                String key      = fuelType + ":" + location;

                // Random walk: ±0.3% mỗi tick (thực tế dầu thô ít biến động hơn VND)
                double drift    = random.nextGaussian() * 0.003;
                double newPrice = currentPrices.get(key) * (1.0 + drift);

                // Giới hạn [80%, 120%] giá cơ sở
                double lowerBound = basePrice * 0.80;
                double upperBound = basePrice * 1.20;
                newPrice = Math.max(lowerBound, Math.min(upperBound, newPrice));

                // Làm tròn: barrel/MMBtu → 2 decimals; gallon → 4 decimals
                newPrice = Math.round(newPrice * 10000.0) / 10000.0;
                currentPrices.put(key, newPrice);

                batch.add(FuelPrice.builder()
                        .timestamp(now)
                        .fuelType(fuelType)
                        .price(newPrice)
                        .priceUnit(priceUnit)
                        .location(location)
                        .region(region)
                        .source("mock-generator")
                        .build());
            }
        }
        return batch;
    }
}
