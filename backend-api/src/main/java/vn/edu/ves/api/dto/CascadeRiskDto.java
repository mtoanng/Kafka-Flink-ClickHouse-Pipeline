package vn.edu.ves.api.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** 1 row của {@code v_cascade_risks}. {@code details} là JSONB → pass-through nguyên gốc. */
@Data
@Builder
@AllArgsConstructor
public class CascadeRiskDto {
    private String riskType;
    private String severity;     // INFO | WARNING | CRITICAL
    private String description;
    /** JSONB string nguyên gốc — frontend tự parse. */
    @JsonRawValue
    private String details;
}
