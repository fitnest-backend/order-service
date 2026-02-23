package az.fitnest.order.dto;

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
public class UpgradeOptionDto {
    private String type; // duration_upgrade, tier_upgrade
    private TargetPackageDto target;
    
    @JsonProperty("payable_difference")
    private BigDecimal payableDifference;
    
    @JsonProperty("new_remaining_limit")
    private Integer newRemainingLimit;
    
    private String badge;
}
