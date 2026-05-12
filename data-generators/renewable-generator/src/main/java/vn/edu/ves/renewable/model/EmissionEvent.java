package vn.edu.ves.renewable.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pillar 4 - Sự kiện phát thải CO2 trong một cửa sổ thời gian.
 *
 * Khớp schema {@code emission_raw} (infra/script/05_init_pillars.sql):
 *   - region_code  : VN_NORTH | VN_CENTRAL | VN_SOUTH
 *   - co2_kg       : tổng CO2 (kg) phát ra trong window
 *   - energy_mwh   : tổng năng lượng (MWh) tạo ra trong window
 *   - event_time   : timestamp cuối window
 *
 * Generated column {@code intensity_kg_per_mwh = co2_kg / energy_mwh}
 * → là metric chính để cảnh báo EMISSION_INTENSITY (>600 kg/MWh = WARNING).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmissionEvent {

    @JsonProperty("region_code")
    private String regionCode;

    @JsonProperty("co2_kg")
    private double co2Kg;

    @JsonProperty("energy_mwh")
    private double energyMwh;

    @JsonProperty("event_time")
    private String eventTime;
}
