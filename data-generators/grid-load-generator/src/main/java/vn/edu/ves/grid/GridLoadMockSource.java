package vn.edu.ves.grid;

import vn.edu.ves.grid.model.GridLoadEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Sinh dữ liệu phụ tải lưới điện 3 vùng VN theo random walk:
 *   - Mỗi vùng có công suất khả dụng cố định (capacity_mw).
 *   - Base load 55-70% capacity tuỳ vùng.
 *   - Giờ cao điểm (18h-22h): +15..25% → có thể vượt 80-95% capacity (ngưỡng WARNING/CRITICAL).
 *   - Random walk ±3% mỗi tick, kẹp trong [40%, 98%] capacity.
 *
 * Số liệu công suất khả dụng (gần đúng theo EVN 2024):
 *   VN_NORTH   ~ 12000 MW
 *   VN_CENTRAL ~  6000 MW
 *   VN_SOUTH   ~ 18000 MW
 */
public class GridLoadMockSource {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final Map<String, Double> CAPACITY_MW = new LinkedHashMap<>();
    static {
        CAPACITY_MW.put("VN_NORTH",   12000.0);
        CAPACITY_MW.put("VN_CENTRAL",  6000.0);
        CAPACITY_MW.put("VN_SOUTH",   18000.0);
    }

    /** Base load % (off-peak) theo từng vùng. */
    private static final Map<String, Double> BASE_LOAD_PCT = new LinkedHashMap<>();
    static {
        BASE_LOAD_PCT.put("VN_NORTH",   0.62);
        BASE_LOAD_PCT.put("VN_CENTRAL", 0.55);
        BASE_LOAD_PCT.put("VN_SOUTH",   0.68);
    }

    private final Map<String, Double> currentLoadMw = new LinkedHashMap<>();
    private final Random random = new Random();

    public GridLoadMockSource() {
        for (Map.Entry<String, Double> e : CAPACITY_MW.entrySet()) {
            double initLoadPct = BASE_LOAD_PCT.get(e.getKey());
            currentLoadMw.put(e.getKey(), e.getValue() * initLoadPct);
        }
    }

    public List<GridLoadEvent> nextBatch() {
        List<GridLoadEvent> batch = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(FORMATTER);
        int hour = now.getHour();
        boolean peak = hour >= 18 && hour < 22;

        for (Map.Entry<String, Double> e : CAPACITY_MW.entrySet()) {
            String region   = e.getKey();
            double capacity = e.getValue();
            double basePct  = BASE_LOAD_PCT.get(region);

            // Target peak boost: +15..25%
            double targetPct = peak
                    ? basePct + 0.15 + random.nextDouble() * 0.10
                    : basePct + (random.nextDouble() - 0.5) * 0.10; // ±5% off-peak

            // Random walk hướng về targetPct (Ornstein-Uhlenbeck-ish)
            double cur = currentLoadMw.get(region);
            double curPct = cur / capacity;
            double drift  = (targetPct - curPct) * 0.20 + (random.nextGaussian() * 0.015);
            double newPct = Math.max(0.40, Math.min(0.98, curPct + drift));

            double newLoad = capacity * newPct;
            newLoad = Math.round(newLoad * 100.0) / 100.0;
            currentLoadMw.put(region, newLoad);

            batch.add(GridLoadEvent.builder()
                    .regionCode(region)
                    .loadMw(newLoad)
                    .capacityMw(capacity)
                    .peakHour(peak)
                    .eventTime(nowStr)
                    .build());
        }
        return batch;
    }
}
