package az.fitnest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagePriceDto {
    private BigDecimal base;
    private BigDecimal discount;
    private BigDecimal effective;
    private String currency;
}
