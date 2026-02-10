package az.fitnest.order.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetPackageDto {
    @JsonProperty("package_id")
    private String packageId;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("duration_months")
    private Integer durationMonths;

    @JsonProperty("target_total_limit")
    private Integer targetTotalLimit;

    @JsonProperty("target_effective_price")
    private BigDecimal targetEffectivePrice;
}
