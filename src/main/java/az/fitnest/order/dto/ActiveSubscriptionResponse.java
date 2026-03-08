package az.fitnest.order.dto;

import lombok.Builder;

@Builder
public record ActiveSubscriptionResponse(
    String order_id,
    String status,
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "dd/MM/yyyy")
    java.time.LocalDate expires_at,
    SubscriptionDetailsDto subscription
) {}
