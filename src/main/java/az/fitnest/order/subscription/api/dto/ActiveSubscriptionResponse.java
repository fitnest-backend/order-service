package az.fitnest.order.subscription.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSubscriptionResponse {
    private String status; // active, none
    private SubscriptionDetailsDto subscription;
}
