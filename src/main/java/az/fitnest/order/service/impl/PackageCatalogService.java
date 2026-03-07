package az.fitnest.order.service.impl;

import az.fitnest.order.dto.*;
import az.fitnest.order.model.entity.*;
import az.fitnest.order.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageCatalogService {

    private final MembershipPlanRepository planRepository;

    /**
     * Returns a flat list where every DurationOption is a separate item.
     * E.g. Bronze 1 Ay, Bronze 3 Ay, Bronze 6 Ay, Bronze 12 Ay — 4 separate cards on mobile.
     */
    @Transactional(readOnly = true)
    public PackageListResponse getAllPackages(boolean activeOnly) {
        List<MembershipPlan> plans = activeOnly ?
                planRepository.findByIsActiveTrue() :
                planRepository.findAll();

        List<SubscriptionPackageDto> dtos = new ArrayList<>();
        for (MembershipPlan plan : plans) {
            if (plan.getOptions() == null || plan.getOptions().isEmpty()) {
                dtos.add(mapToDto(plan, null));
            } else {
                for (DurationOption option : plan.getOptions()) {
                    dtos.add(mapToDto(plan, option));
                }
            }
        }

        return PackageListResponse.builder()
                .items(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageDto getPackageByOptionId(Long optionId) {
        for (MembershipPlan plan : planRepository.findAll()) {
            for (DurationOption option : plan.getOptions()) {
                if (option.getId().equals(optionId)) {
                    return mapToDto(plan, option);
                }
            }
        }
        throw new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found");
    }

    private SubscriptionPackageDto mapToDto(MembershipPlan plan, DurationOption option) {
        PackagePriceDto priceDto = null;
        String badge = null;
        Integer visitLimit = 0;
        Integer freezeDays = 0;
        Integer durationMonths = null;
        Long optionId = null;
        List<PackageServiceDto> services = List.of();

        if (option != null) {
            optionId = option.getId();
            durationMonths = option.getDurationMonths();
            BigDecimal base = option.getPriceStandard();
            BigDecimal discount = option.getPriceDiscounted();
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

            visitLimit = option.getEntryLimit() != null ? option.getEntryLimit() : 0;
            freezeDays = option.getFreezeDays() != null ? option.getFreezeDays() : 0;

            if (option.getServices() != null) {
                services = option.getServices().stream()
                        .map(s -> PackageServiceDto.builder().serviceName(s.getName()).build())
                        .collect(Collectors.toList());
            }
        }

        return SubscriptionPackageDto.builder()
                .packageId(plan.getId().toString())
                .optionId(optionId)
                .name(plan.getName())
                .durationMonths(durationMonths)
                .isActive(plan.getIsActive())
                .price(priceDto)
                .badge(badge)
                .visitLimit(visitLimit)
                .freezeDays(freezeDays)
                .services(services)
                .discountPercent(plan.getServiceDiscountPercent())
                .build();
    }
}
