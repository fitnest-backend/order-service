package az.fitnest.order.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record UpgradeOptionsResponse(
    SubscriptionDetailsDto current,
    List<UpgradeOptionDto> options
) {}
