package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 row của {@code fuel_prices_raw} (lấy mới nhất theo limit). */
@Data
@Builder
@AllArgsConstructor
public class FuelPriceDto {
    private long          id;
    private LocalDateTime eventTimestamp;
    private String        fuelType;
    private BigDecimal    price;
    private String        priceUnit;
    private String        location;
    private String        region;
    private String        source;
}
