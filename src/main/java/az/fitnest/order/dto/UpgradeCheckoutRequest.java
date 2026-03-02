package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpgradeCheckoutRequest(
    @JsonProperty("current_subscription_id")
    @NotNull(message = "Mövcud abunəlik ID-si mütləqdir")
    Long currentSubscriptionId,

    @JsonProperty("target_package_id")
    @NotNull(message = "Hədəf paket ID-si mütləqdir")
    String targetPackageId,

    @JsonProperty("target_duration_months")
    @NotNull(message = "Hədəf müddət mütləqdir")
    Integer targetDurationMonths,

    @JsonProperty("payment_method_id")
    @NotNull(message = "Ödəniş üsulu ID-si mütləqdir")
    String paymentMethodId
) {}
