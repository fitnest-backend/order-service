package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record TargetPackageDto(
    @JsonProperty("package_id")
    String packageId,
    @JsonProperty("option_id")
    Long optionId,
    @JsonProperty("package_name")
    String packageName,
    @JsonProperty("duration_months")
    Integer durationMonths,
    @JsonProperty("target_total_limit")
    Integer targetTotalLimit,
    @JsonProperty("target_effective_price")
    BigDecimal targetEffectivePrice
) {}
