package vn.edu.ves.renewable.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pillar 4 - Sự kiện sản lượng năng lượng tái tạo.
 *
 * Khớp schema {@code renewable_output_raw} (infra/script/05_init_pillars.sql):
 *   - region_code  : VN_NORTH | VN_CENTRAL | VN_SOUTH
 *   - source_type  : SOLAR | WIND | HYDRO  (CHECK constraint)
 *   - output_mw    : MW phát ra tại thời điểm event_time
 *   - capacity_mw  : công suất lắp đặt
 *   - event_time   : ISO-8601 timestamp
 *
 * Cột generated {@code utilization_pct = output_mw / capacity_mw * 100} do Postgres tự tính.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewableOutputEvent {

    @JsonProperty("region_code")
    private String regionCode;

    /** SOLAR | WIND | HYDRO */
    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("output_mw")
    private double outputMw;

    @JsonProperty("capacity_mw")
    private double capacityMw;

    @JsonProperty("event_time")
    private String eventTime;
}
