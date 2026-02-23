package az.fitnest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DurationOptionEntityDto {
    private Long id;
    private Integer durationMonths;
    private BigDecimal priceStandard;
    private BigDecimal priceDiscounted;
    private Integer entryLimit;
    private Integer freezeDays;
    private List<String> services;
}
