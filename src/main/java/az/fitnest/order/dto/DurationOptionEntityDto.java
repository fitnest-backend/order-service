package az.fitnest.order.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record DurationOptionEntityDto(
    Long id,
    Integer durationMonths,
    BigDecimal priceStandard,
    BigDecimal priceDiscounted,
    Integer entryLimit,
    Integer freezeDays,
    List<String> services
) {}
