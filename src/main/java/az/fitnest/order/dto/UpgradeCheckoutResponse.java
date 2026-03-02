package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpgradeCheckoutResponse(
    @JsonProperty("order_id")
    String orderId,
    PaymentResultDto payment,
    SubscriptionDetailsDto subscription,
    @JsonProperty("subscription_unchanged")
    Boolean subscriptionUnchanged
) {}
