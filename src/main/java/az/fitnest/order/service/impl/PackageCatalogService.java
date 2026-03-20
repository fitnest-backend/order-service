package az.fitnest.order.service.impl;

import az.fitnest.order.dto.*;
import az.fitnest.order.model.entity.*;
import az.fitnest.order.repository.SubscriptionPackageRepository;
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

    private final SubscriptionPackageRepository packageRepository;

    @Transactional(readOnly = true)
    public PackageListResponse getAllPackages(boolean activeOnly) {
        List<SubscriptionPackage> packages = activeOnly ?
                packageRepository.findByIsActiveTrue() :
                packageRepository.findAll();

        List<SubscriptionPackageDto> dtos = new ArrayList<>();
        for (SubscriptionPackage pkg : packages) {
            if (pkg.getOptions() == null || pkg.getOptions().isEmpty()) {
                dtos.add(mapToDto(pkg, null));
            } else {
                for (PackageOption option : pkg.getOptions()) {
                    dtos.add(mapToDto(pkg, option));
                }
            }
        }

        return PackageListResponse.builder()
                .items(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public PackagePlanListResponse getUniquePlans() {
        List<SubscriptionPackage> packages = packageRepository.findAll();

        List<SubscriptionPackageResponse> dtos = packages.stream()
                .map(this::mapToPackageResponse)
                .collect(Collectors.toList());

        return PackagePlanListResponse.builder()
                .items(dtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PackageNameDto> getPackageNames() {
        return packageRepository.findAll().stream()
                .map(pkg -> PackageNameDto.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageResponse getPlanById(Long packageId) {
        SubscriptionPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));
        return mapToPackageResponse(pkg);
    }

    @Transactional(readOnly = true)
    public List<PackageOptionDto> getOptionsByPlanId(Long packageId) {
        SubscriptionPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));
        return pkg.getOptions().stream()
                .map(o -> mapToOptionDto(pkg, o))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageDto getOptionDetails(Long packageId, Long optionId) {
        SubscriptionPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        PackageOption option = pkg.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.option_not_found"));

        return mapToDto(pkg, option);
    }

    @Transactional(readOnly = true)
    public SubscriptionPackageDto getPackageByOptionId(Long optionId) {
        for (SubscriptionPackage pkg : packageRepository.findAll()) {
            for (PackageOption option : pkg.getOptions()) {
                if (option.getId().equals(optionId)) {
                    return mapToDto(pkg, option);
                }
            }
        }
        throw new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found");
    }

    private SubscriptionPackageResponse mapToPackageResponse(SubscriptionPackage pkg) {
        List<PackageOptionDto> options = pkg.getOptions().stream()
                .map(o -> mapToOptionDto(pkg, o))
                .collect(Collectors.toList());

        return SubscriptionPackageResponse.builder()
                .packageId(pkg.getId().toString())
                .name(pkg.getName())
                .isActive(pkg.getIsActive())
                .discountPercent(pkg.getServiceDiscountPercent())
                .options(options)
                .build();
    }

    private PackageOptionDto mapToOptionDto(SubscriptionPackage pkg, PackageOption option) {
        BigDecimal base = option.getPriceStandard();
        BigDecimal discount = option.getPriceDiscounted();
        BigDecimal effective = discount != null ? discount : base;

        PackagePriceDto priceDto = PackagePriceDto.builder()
                .base(base)
                .discount(discount)
                .effective(effective)
                .currency(pkg.getCurrency())
                .build();

        String badge = (discount != null && base != null && discount.compareTo(base) < 0) ? "discount" : null;

        List<PackageBenefitDto> benefits = option.getBenefits() != null ?
                option.getBenefits().stream()
                        .map(b -> PackageBenefitDto.builder()
                                .description(b.getDescription())
                                .build())
                        .collect(Collectors.toList()) :
                List.of();

        return PackageOptionDto.builder()
                .optionId(option.getId())
                .durationMonths(option.getDurationMonths())
                .price(priceDto)
                .badge(badge)
                .visitLimit(option.getEntryLimit() != null ? option.getEntryLimit() : 0)
                .freezeDays(option.getFreezeDays() != null ? option.getFreezeDays() : 0)
                .benefits(benefits)
                .build();
    }

    private SubscriptionPackageDto mapToDto(SubscriptionPackage pkg, PackageOption option) {
        PackagePriceDto priceDto = null;
        String badge = null;
        Integer visitLimit = 0;
        Integer freezeDays = 0;
        Integer durationMonths = null;
        Long optionId = null;
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
                    .currency(pkg.getCurrency())
                    .build();

            if (discount != null && base != null && discount.compareTo(base) < 0) {
                badge = "discount";
            }

            visitLimit = option.getEntryLimit() != null ? option.getEntryLimit() : 0;
            freezeDays = option.getFreezeDays() != null ? option.getFreezeDays() : 0;

            if (option.getBenefits() != null) {
                benefits = option.getBenefits().stream()
                        .map(b -> PackageBenefitDto.builder()
                                .description(b.getDescription())
                                .build())
                        .collect(Collectors.toList());
            }
        }

        return SubscriptionPackageDto.builder()
                .packageId(pkg.getId().toString())
                .optionId(optionId)
                .name(pkg.getName())
                .durationMonths(durationMonths)
                .isActive(pkg.getIsActive())
                .price(priceDto)
                .badge(badge)
                .visitLimit(visitLimit)
                .freezeDays(freezeDays)
                .benefits(benefits)
                .build();
    }
}
