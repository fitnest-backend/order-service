package az.fitnest.order.service.impl;

import az.fitnest.order.dto.*;
import az.fitnest.order.entity.*;
import az.fitnest.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageCatalogService {

    private final SubscriptionPackageRepository packageRepository;
    private final PackageDurationRepository durationRepository;
    private final PackagePricingRepository pricingRepository;
    private final PackageVisitLimitRepository visitLimitRepository;
    private final PackageFreezeRuleRepository freezeRuleRepository;
    private final PackageServiceOptionRepository serviceOptionRepository;

    @Transactional(readOnly = true)
    public PackageListResponse getAllPackages(boolean activeOnly) {
        List<SubscriptionPackage> packages = activeOnly ? 
                packageRepository.findByIsActiveTrue() : 
                packageRepository.findAll();

        List<SubscriptionPackageDto> dtos = packages.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PackageListResponse.builder()
                .items(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageDto getPackageById(String packageId) {
        SubscriptionPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found: " + packageId)); // TODO: Custom exception
        return mapToDto(pkg);
    }

    private SubscriptionPackageDto mapToDto(SubscriptionPackage pkg) {
        String packageId = pkg.getPackageId();
        
        // Fetch related data
        List<PackageDuration> durations = durationRepository.findByPackageId(packageId);
        List<PackageServiceOption> services = serviceOptionRepository.findByPackageId(packageId);
        
        // For preview, we usually pick the base properties or the 1-month properties
        // The spec implies listing durations, but price/limits are singular in the example response.
        // "flattened preview of durations, prices, limits and benefits"
        // Example response shows "durations: [{duration_months: 1}]" and "price: {base: 75...}".
        // It seems it shows the DEFAULT option (e.g. 1 month) or maybe it iterates. 
        // Spec 5.1 response has `items` which are package objects.
        // It seems the preview shows the "starting at" or 1-month configuration.
        
        // Let's assume we show the 1-month configuration or the first available duration for the "top level" fields
        // like price, visit_limit, freeze_days.
        
        Integer defaultDuration = 1;
        
        PackagePricing pricing = pricingRepository.findByPackageIdAndDurationMonths(packageId, defaultDuration)
                .orElse(null);
        PackageVisitLimit limit = visitLimitRepository.findByPackageIdAndDurationMonths(packageId, defaultDuration)
                .orElse(null);
        PackageFreezeRule freeze = freezeRuleRepository.findByPackageIdAndDurationMonths(packageId, defaultDuration)
                .orElse(null);

        // Map Price
        PackagePriceDto priceDto = null;
        String badge = null;
        if (pricing != null) {
            priceDto = PackagePriceDto.builder()
                    .base(pricing.getBasePrice())
                    .discount(pricing.getDiscountPrice())
                    .effective(pricing.getDiscountPrice() != null ? pricing.getDiscountPrice() : pricing.getBasePrice())
                    .currency(pricing.getCurrency())
                    .build();
            
            if (pricing.getDiscountPrice() != null && pricing.getDiscountPrice().compareTo(pricing.getBasePrice()) < 0) {
                badge = "discount";
            }
        }

        // Map Services
        List<PackageServiceDto> serviceDtos = services.stream()
                .map(s -> PackageServiceDto.builder().serviceName(s.getServiceName()).build())
                .collect(Collectors.toList());
        
        // Calculate max discount percent from services? Spec 5.1 says "discount_percent: 5".
        // PackageServiceOption has discountPercent. Let's take the max? Or separate service discounts?
        // Spec 2.1 "Package_Services — services included in a package and optional discount percent"
        // Example response has top-level discount_percent. Let's sum them or take the first?
        // Let's take the first non-null for now or max.
        BigDecimal discountPercent = services.stream()
                .map(PackageServiceOption::getDiscountPercent)
                .filter(d -> d != null)
                .findFirst().orElse(null);

        return SubscriptionPackageDto.builder()
                .packageId(pkg.getPackageId())
                .name(pkg.getName())
                .isActive(pkg.getIsActive())
                .durations(durations.stream()
                        .map(d -> PackageDurationDto.builder().durationMonths(d.getDurationMonths()).build())
                        .collect(Collectors.toList()))
                .price(priceDto)
                .badge(badge)
                .visitLimit(limit != null ? limit.getVisitLimit() : 0)
                .freezeDays(freeze != null ? freeze.getFreezeDays() : 0)
                .services(serviceDtos)
                .discountPercent(discountPercent)
                .build();
    }
}
