package vn.edu.ves.api.dto;

import lombok.Data;

/** Body cho POST /api/recommendations/{id}/acknowledge. */
@Data
public class AckRequest {
    /** ACKNOWLEDGED (mặc định) | DISMISSED. */
    private String status = "ACKNOWLEDGED";
    private String note;
}
