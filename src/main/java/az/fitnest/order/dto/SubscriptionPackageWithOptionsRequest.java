package az.fitnest.order.dto;

import az.fitnest.order.model.enums.BillingPeriod;
import java.util.List;
import lombok.Builder;

@Builder
public record SubscriptionPackageWithOptionsRequest(
    String name,
    String currency,
    BillingPeriod billingPeriod,
    Boolean isActive,
    Integer sortOrder,
    List<PackageOptionEntityDto> options
) {}
