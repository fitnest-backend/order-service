package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeCheckoutRequest {
    @NotNull
    @JsonProperty("current_subscription_id")
    private Long currentSubscriptionId;

    @NotNull
    @JsonProperty("target_package_id")
    private String targetPackageId;

    @NotNull
    @JsonProperty("target_duration_months")
    private Integer targetDurationMonths;

    @NotNull
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
}
