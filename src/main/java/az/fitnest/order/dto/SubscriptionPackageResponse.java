package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Builder
public record SubscriptionPackageResponse(
    @JsonProperty("package_id")
    String packageId,
    String name,
    @JsonProperty("is_active")
    Boolean isActive,
    List<PackageOptionDto> options
) {}
