package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record SubscriptionPackageDto(
    @JsonProperty("package_id")
    String packageId,

    @JsonProperty("option_id")
    Long optionId,

    String name,

    @JsonProperty("duration_months")
    Integer durationMonths,

    @JsonProperty("is_active")
    Boolean isActive,

    PackagePriceDto price,
    String badge,

    @JsonProperty("visit_limit")
    Integer visitLimit,

    @JsonProperty("freeze_days")
    Integer freezeDays,

    List<PackageServiceDto> services,

    @JsonProperty("discount_percent")
    BigDecimal discountPercent
) {}
