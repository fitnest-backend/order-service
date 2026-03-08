package az.fitnest.order.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record PackagePlanListResponse(
    List<SubscriptionPackageResponse> items
) {}
