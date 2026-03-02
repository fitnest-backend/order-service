package az.fitnest.order.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PackagePriceDto(
    BigDecimal base,
    BigDecimal discount,
    BigDecimal effective,
    String currency
) {}
