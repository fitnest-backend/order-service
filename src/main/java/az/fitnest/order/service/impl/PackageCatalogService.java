package az.fitnest.order.service.impl;

import az.fitnest.order.dto.*;
import az.fitnest.order.entity.*;
import az.fitnest.order.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageCatalogService {

    private final MembershipPlanRepository planRepository;

    @Transactional(readOnly = true)
    public PackageListResponse getAllPackages(boolean activeOnly) {
        List<MembershipPlan> plans = activeOnly ?
                planRepository.findByIsActiveTrue() :
                planRepository.findAll();

        List<SubscriptionPackageDto> dtos = plans.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PackageListResponse.builder()
                .items(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageDto getPackageById(Long planId) {
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
        return mapToDto(plan);
    }

    private SubscriptionPackageDto mapToDto(MembershipPlan plan) {
        // Pick the first/default duration option for the representative preview
        DurationOption defaultOption = plan.getOptions().stream()
                .filter(o -> o.getDurationMonths() != null && o.getDurationMonths() == 1)
                .findFirst()
                .orElse(plan.getOptions().isEmpty() ? null : plan.getOptions().get(0));

        // Map price from the default option
        PackagePriceDto priceDto = null;
        String badge = null;
        if (defaultOption != null) {
            BigDecimal base = defaultOption.getPriceStandard();
            BigDecimal discount = defaultOption.getPriceDiscounted();
            BigDecimal effective = discount != null ? discount : base;

            priceDto = PackagePriceDto.builder()
                    .base(base)
                    .discount(discount)
                    .effective(effective)
                    .currency(plan.getCurrency())
                    .build();

            if (discount != null && base != null && discount.compareTo(base) < 0) {
                badge = "discount";
            }
        }

        // Map durations from DurationOptions
        List<PackageDurationDto> durations = plan.getOptions().stream()
                .map(o -> PackageDurationDto.builder().durationMonths(o.getDurationMonths()).build())
                .collect(Collectors.toList());

        // Map services from the default option
        List<PackageServiceDto> services = List.of();
        if (defaultOption != null && defaultOption.getServices() != null) {
            services = defaultOption.getServices().stream()
                    .map(s -> PackageServiceDto.builder().serviceName(s).build())
                    .collect(Collectors.toList());
        }

        return SubscriptionPackageDto.builder()
                .packageId(plan.getId().toString())
                .name(plan.getName())
                .isActive(plan.getIsActive())
                .durations(durations)
                .price(priceDto)
                .badge(badge)
                .visitLimit(defaultOption != null && defaultOption.getEntryLimit() != null ? defaultOption.getEntryLimit() : 0)
                .freezeDays(defaultOption != null && defaultOption.getFreezeDays() != null ? defaultOption.getFreezeDays() : 0)
                .services(services)
                .discountPercent(plan.getServiceDiscountPercent())
                .build();
    }
}
