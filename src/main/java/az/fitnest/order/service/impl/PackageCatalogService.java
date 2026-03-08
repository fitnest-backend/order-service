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
    public PackagePlanListResponse getUniquePlans() {
        List<MembershipPlan> plans = planRepository.findAll();

        List<SubscriptionPackageResponse> dtos = plans.stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());

        return PackagePlanListResponse.builder()
                .items(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageResponse getPlanById(Long packageId) {
        MembershipPlan plan = planRepository.findById(packageId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));
        return mapToPlanResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PackageOptionDto> getOptionsByPlanId(Long packageId) {
        MembershipPlan plan = planRepository.findById(packageId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));
        return plan.getOptions().stream()
                .map(o -> mapToOptionDto(plan, o))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageDto getOptionDetails(Long packageId, Long optionId) {
        MembershipPlan plan = planRepository.findById(packageId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));
        
        DurationOption option = plan.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.option_not_found"));

        return mapToDto(plan, option);
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

    private SubscriptionPackageResponse mapToPlanResponse(MembershipPlan plan) {
        List<PackageBenefitDto> benefits = plan.getBenefits().stream()
                .map(b -> PackageBenefitDto.builder()
                        .logo(b.getLogo())
                        .description(b.getDescription())
                        .build())
                .collect(Collectors.toList());

        List<PackageOptionDto> options = plan.getOptions().stream()
                .map(o -> mapToOptionDto(plan, o))
                .collect(Collectors.toList());

        return SubscriptionPackageResponse.builder()
                .packageId(plan.getId().toString())
                .name(plan.getName())
                .isActive(plan.getIsActive())
                .discountPercent(plan.getServiceDiscountPercent())
                .benefits(benefits)
                .options(options)
                .build();
    }

    private PackageOptionDto mapToOptionDto(MembershipPlan plan, DurationOption option) {
        BigDecimal base = option.getPriceStandard();
        BigDecimal discount = option.getPriceDiscounted();
        BigDecimal effective = discount != null ? discount : base;

        PackagePriceDto priceDto = PackagePriceDto.builder()
                .base(base)
                .discount(discount)
                .effective(effective)
                .currency(plan.getCurrency())
                .build();

        String badge = (discount != null && base != null && discount.compareTo(base) < 0) ? "discount" : null;

        List<PackageServiceDto> services = option.getServices() != null ?
                option.getServices().stream()
                        .map(s -> PackageServiceDto.builder().serviceName(s.getName()).build())
                        .collect(Collectors.toList()) :
                List.of();

        return PackageOptionDto.builder()
                .optionId(option.getId())
                .durationMonths(option.getDurationMonths())
                .price(priceDto)
                .badge(badge)
                .visitLimit(option.getEntryLimit() != null ? option.getEntryLimit() : 0)
                .freezeDays(option.getFreezeDays() != null ? option.getFreezeDays() : 0)
                .services(services)
                .build();
    }

    private SubscriptionPackageDto mapToDto(MembershipPlan plan, DurationOption option) {
        PackagePriceDto priceDto = null;
        String badge = null;
        Integer visitLimit = 0;
        Integer freezeDays = 0;
        Integer durationMonths = null;
        Long optionId = null;
        List<PackageServiceDto> services = List.of();
        List<PackageBenefitDto> benefits = List.of();

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

        if (plan.getBenefits() != null) {
            benefits = plan.getBenefits().stream()
                    .map(b -> PackageBenefitDto.builder()
                            .logo(b.getLogo())
                            .description(b.getDescription())
                            .build())
                    .collect(Collectors.toList());
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
                .benefits(benefits)
                .build();
    }
}
