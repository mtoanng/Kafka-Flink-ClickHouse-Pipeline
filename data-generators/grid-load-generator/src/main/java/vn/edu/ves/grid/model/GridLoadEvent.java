package vn.edu.ves.grid.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pillar 3 - Sự kiện phụ tải lưới điện theo vùng VN.
 *
 * Khớp schema {@code grid_load_raw} (xem infra/script/05_init_pillars.sql):
 *   - region_code   : VN_NORTH | VN_CENTRAL | VN_SOUTH (FK regions(code))
 *   - load_mw       : phụ tải hiện tại (MW)
 *   - capacity_mw   : công suất khả dụng tối đa (MW)
 *   - is_peak_hour  : có nằm trong khung giờ cao điểm (18-22h) không
 *   - event_time    : ISO-8601 timestamp khi sự kiện sinh ra
 *
 * Cột generated {@code load_pct = load_mw / capacity_mw * 100} sẽ do Postgres tự tính.
 *
 * JSON mẫu:
 * {
 *   "region_code":  "VN_NORTH",
 *   "load_mw":      9420.5,
 *   "capacity_mw":  12000.0,
 *   "is_peak_hour": true,
 *   "event_time":   "2026-05-12T19:05:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridLoadEvent {

    @JsonProperty("region_code")
    private String regionCode;

    @JsonProperty("load_mw")
    private double loadMw;

    @JsonProperty("capacity_mw")
    private double capacityMw;

    @JsonProperty("is_peak_hour")
    private boolean peakHour;

    @JsonProperty("event_time")
    private String eventTime;
}
