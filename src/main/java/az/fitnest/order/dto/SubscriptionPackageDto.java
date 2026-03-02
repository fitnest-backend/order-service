package az.fitnest.order.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record SubscriptionPackageDto(
    String packageId,
    String name,
    Boolean isActive,
    List<PackageDurationDto> durations,
    PackagePriceDto price,
    String badge,
    Integer visitLimit,
    Integer freezeDays,
    List<PackageServiceDto> services,
    BigDecimal discountPercent
) {}
