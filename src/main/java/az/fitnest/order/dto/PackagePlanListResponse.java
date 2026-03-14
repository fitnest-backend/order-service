package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Builder
public record PackagePlanListResponse(
    List<SubscriptionPackageResponse> items
) {}
