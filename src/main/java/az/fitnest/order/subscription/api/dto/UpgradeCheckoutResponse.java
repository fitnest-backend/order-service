package az.fitnest.order.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeCheckoutResponse {
    @JsonProperty("order_id")
    private String orderId;
    
    private PaymentResultDto payment;
    
    private SubscriptionDetailsDto subscription;
    
    @JsonProperty("subscription_unchanged")
    private Boolean subscriptionUnchanged;
}
