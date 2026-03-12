package az.fitnest.order.dto;

import az.fitnest.order.model.enums.BillingPeriod;
import lombok.Builder;

@Builder
public record SubscriptionPackageBasicRequest(
    String name,
    String currency,
    BillingPeriod billingPeriod,
    Boolean isActive
) {}
