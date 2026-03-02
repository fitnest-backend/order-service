package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpgradeOptionDto(
    String type,
    TargetPackageDto target,
    @JsonProperty("payable_difference")
    BigDecimal payableDifference,
    @JsonProperty("new_remaining_limit")
    Integer newRemainingLimit,
    String badge
) {}
