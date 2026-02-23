package az.fitnest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackageDto {
    private String packageId;
    private String name;
    private Boolean isActive;
    private List<PackageDurationDto> durations;
    private PackagePriceDto price; // Representative price (e.g. for 1 month or lowest)
    private String badge;
    private Integer visitLimit;
    private Integer freezeDays;
    private List<PackageServiceDto> services;
    private BigDecimal discountPercent;
}
