package vn.edu.ves.renewable;

import vn.edu.ves.renewable.model.EmissionEvent;
import vn.edu.ves.renewable.model.RenewableOutputEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Sinh dữ liệu cho Pillar 4 (Chuyển đổi & Môi trường):
 *
 *  - SOLAR    : công suất theo giờ trong ngày → bell-curve quanh 12h trưa,
 *               output = 0 từ 18h-6h.
 *  - WIND     : 24/7, biên độ thấp hơn solar, có biến động random.
 *  - HYDRO    : 24/7, gần như ổn định, dao động ±10%.
 *
 * Tổng năng lượng tái tạo trong tick + tổng load lưới (giả định 75% capacity)
 * dùng để suy ra phát thải CO2:
 *   non_renewable_mwh = max(0, total_load_mwh - total_renewable_mwh)
 *   intensity = 750 kg/MWh (mặc định nhiệt điện than/khí)
 *   co2_kg = non_renewable_mwh * intensity
 * → khi renewable cao thì intensity_kg_per_mwh thấp (đúng logic Net Zero).
 *
 * Capacity tham chiếu (theo dự thảo PDP8 2024-2030):
 *   VN_NORTH   : Solar 1500 / Wind 600 / Hydro 2000  MW
 *   VN_CENTRAL : Solar 1800 / Wind 1200 / Hydro 1500 MW   (gió mạnh ở Ninh Thuận/Bình Thuận)
 *   VN_SOUTH   : Solar 2500 / Wind 800  / Hydro 1000 MW   (solar mạnh ở ĐBSCL)
 */
public class RenewableMockSource {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Default intensity (kg CO2/MWh) khi phải đốt nhiệt điện than/khí bù vào. */
    private static final double NON_RENEWABLE_INTENSITY = 750.0;

    /** Giả định lưới đang chạy ở 75% capacity (off-peak avg). */
    private static final double ASSUMED_GRID_LOAD_PCT = 0.75;

    /** Capacity grid tổng cho mỗi region (đồng nhất với grid-load-generator). */
    private static final Map<String, Double> GRID_CAPACITY_MW = new LinkedHashMap<>();
    static {
        GRID_CAPACITY_MW.put("VN_NORTH",   12000.0);
        GRID_CAPACITY_MW.put("VN_CENTRAL",  6000.0);
        GRID_CAPACITY_MW.put("VN_SOUTH",   18000.0);
    }

    /** Capacity renewable: region -> (source_type -> capacity_mw). */
    private static final Map<String, Map<String, Double>> RENEWABLE_CAPACITY = new LinkedHashMap<>();
    static {
        RENEWABLE_CAPACITY.put("VN_NORTH",   buildCap(1500, 600,  2000));
        RENEWABLE_CAPACITY.put("VN_CENTRAL", buildCap(1800, 1200, 1500));
        RENEWABLE_CAPACITY.put("VN_SOUTH",   buildCap(2500, 800,  1000));
    }

    private static Map<String, Double> buildCap(double solar, double wind, double hydro) {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("SOLAR", solar);
        m.put("WIND",  wind);
        m.put("HYDRO", hydro);
        return m;
    }

    private final Random random = new Random();

    public List<RenewableOutputEvent> nextOutputBatch() {
        List<RenewableOutputEvent> out = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(FORMATTER);
        int hour = now.getHour();

        double solarFactor = solarUtilizationCurve(hour);
        double windFactor  = 0.35 + random.nextDouble() * 0.45;   // 35-80% capacity
        double hydroFactor = 0.55 + random.nextDouble() * 0.20;   // 55-75% capacity

        for (Map.Entry<String, Map<String, Double>> region : RENEWABLE_CAPACITY.entrySet()) {
            String regionCode = region.getKey();
            for (Map.Entry<String, Double> src : region.getValue().entrySet()) {
                String sourceType = src.getKey();
                double capacity   = src.getValue();
                double util;
                switch (sourceType) {
                    case "SOLAR": util = solarFactor; break;
                    case "WIND":  util = windFactor + (random.nextGaussian() * 0.05); break;
                    case "HYDRO": util = hydroFactor + (random.nextGaussian() * 0.02); break;
                    default:      util = 0.0;
                }
                util = Math.max(0.0, Math.min(1.0, util));
                double outputMw = Math.round(capacity * util * 100.0) / 100.0;

                out.add(RenewableOutputEvent.builder()
                        .regionCode(regionCode)
                        .sourceType(sourceType)
                        .outputMw(outputMw)
                        .capacityMw(capacity)
                        .eventTime(nowStr)
                        .build());
            }
        }
        return out;
    }

    /**
     * Sinh CO2 emission cho mỗi region.
     *
     * @param renewableOutputs lô output renewable hiện tại (cùng tick) để suy ra
     *                         phần năng lượng phải bù bằng nhiệt điện.
     * @param windowMinutes    chiều dài window dùng để quy đổi MW → MWh.
     */
    public List<EmissionEvent> nextEmissionBatch(List<RenewableOutputEvent> renewableOutputs,
                                                 double windowMinutes) {
        List<EmissionEvent> out = new ArrayList<>();
        String nowStr = LocalDateTime.now().format(FORMATTER);
        double windowHours = windowMinutes / 60.0;

        for (Map.Entry<String, Double> region : GRID_CAPACITY_MW.entrySet()) {
            String regionCode = region.getKey();
            double gridCapacity = region.getValue();
            double totalLoadMw = gridCapacity * ASSUMED_GRID_LOAD_PCT;

            double totalRenewableMw = renewableOutputs.stream()
                    .filter(e -> regionCode.equals(e.getRegionCode()))
                    .mapToDouble(RenewableOutputEvent::getOutputMw)
                    .sum();

            double nonRenewableMw = Math.max(0.0, totalLoadMw - totalRenewableMw);

            // MW * hour = MWh; sample tần suất window phút → quy đổi đúng
            double energyMwh    = Math.round(totalLoadMw    * windowHours * 100.0) / 100.0;
            double nonRenewMwh  = nonRenewableMw * windowHours;

            // Add slight noise ±3% to intensity to reflect real-life variation
            double intensityNoise = 1.0 + (random.nextGaussian() * 0.03);
            double co2Kg = Math.round(nonRenewMwh * NON_RENEWABLE_INTENSITY * intensityNoise * 100.0) / 100.0;
            if (co2Kg < 0) co2Kg = 0.0;

            out.add(EmissionEvent.builder()
                    .regionCode(regionCode)
                    .co2Kg(co2Kg)
                    .energyMwh(energyMwh)
                    .eventTime(nowStr)
                    .build());
        }
        return out;
    }

    /**
     * Bell-curve gần đúng cho solar:
     *   - 0% từ 18h-6h
     *   - đỉnh ~85% lúc 12h
     *   - giảm dần về cả hai phía
     * + nhiễu mây ngẫu nhiên ±15%.
     */
    private double solarUtilizationCurve(int hour) {
        if (hour < 6 || hour >= 18) return 0.0;
        // Khoảng cách so với 12h, chuẩn hoá về [0..1]
        double dx = Math.abs(hour - 12) / 6.0; // 6h hoặc 18h → dx=1.0
        double base = Math.cos((Math.PI / 2.0) * dx); // cos(0)=1, cos(pi/2)=0
        double peak = base * 0.85; // tối đa 85% capacity
        double cloudNoise = (random.nextDouble() - 0.5) * 0.30; // ±15%
        return Math.max(0.0, peak + cloudNoise);
    }
}
