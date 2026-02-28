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
    @NotNull(message = "Mövcud abunəlik ID-si mütləqdir")
    @JsonProperty("current_subscription_id")
    private Long currentSubscriptionId;

    @NotNull(message = "Hədəf paket ID-si mütləqdir")
    @JsonProperty("target_package_id")
    private String targetPackageId;

    @NotNull(message = "Hədəf müddət mütləqdir")
    @JsonProperty("target_duration_months")
    private Integer targetDurationMonths;

    @NotNull(message = "Ödəniş üsulu ID-si mütləqdir")
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
}
