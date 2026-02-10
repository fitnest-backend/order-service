package az.fitnest.order.subscription.adapter.service;

import az.fitnest.order.subscription.api.dto.*;
import az.fitnest.order.subscription.domain.model.*;
import az.fitnest.order.shared.exception.ServiceException;
import az.fitnest.order.subscription.adapter.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpgradeService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPackageRepository packageRepository;
    private final PackagePricingRepository pricingRepository;
    private final PackageVisitLimitRepository visitLimitRepository;
    private final PackageDurationRepository durationRepository;
    private final MockPaymentService paymentService;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public UpgradeOptionsResponse getUpgradeOptions(Long userId, Integer targetDurationMonths) {
        Subscription currentSub = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ServiceException("User has no active subscription", org.springframework.http.HttpStatus.CONFLICT, "NO_ACTIVE_SUBSCRIPTION")); 

        // Infer current duration (see UserSubscriptionService)
        // Ideally we should store this. For now, matching DB logic.
        long currentDurationLong = 1;
        if (currentSub.getEndAt() != null) {
            currentDurationLong = java.time.temporal.ChronoUnit.MONTHS.between(currentSub.getStartAt(), currentSub.getEndAt());
            if (currentDurationLong == 0) currentDurationLong = 1;
        }
        int currentDuration = (int) currentDurationLong;

        // Get current pricing
        PackagePricing currentPricing = pricingRepository.findByPackageIdAndDurationMonths(currentSub.getPackageId(), currentDuration)
                .orElseThrow(() -> new ServiceException("Current package configuration not found", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"));
        BigDecimal currentEffectivePrice = currentPricing.getDiscountPrice() != null ? currentPricing.getDiscountPrice() : currentPricing.getBasePrice();

        SubscriptionPackage currentPackage = packageRepository.findById(currentSub.getPackageId()).orElseThrow();

        // Prepare current details for response
        SubscriptionDetailsDto currentDetails = SubscriptionDetailsDto.builder()
                .subscriptionId(currentSub.getSubscriptionId())
                .packageId(currentSub.getPackageId())
                .packageName(currentPackage.getName())
                .durationMonths(currentDuration)
                .effectivePrice(currentEffectivePrice)
                .currency(currentPricing.getCurrency())
                .remainingLimit(currentSub.getRemainingLimit())
                .build();

        // Find Candidates
        List<UpgradeOptionDto> options = new ArrayList<>();
        List<SubscriptionPackage> allPackages = packageRepository.findByIsActiveTrue();

        for (SubscriptionPackage pkg : allPackages) {
            List<PackageDuration> durations = durationRepository.findByPackageId(pkg.getPackageId());
            
            for (PackageDuration d : durations) {
                // Determine if upgrade scenario
                boolean isSamePackage = pkg.getPackageId().equals(currentSub.getPackageId());
                boolean isDurationUpgrade = isSamePackage && d.getDurationMonths() > currentDuration;
                
                // For tier upgrade (different package), we generally look for higher price.
                // Spec says "Upgrade to a higher tier package".
                PackagePricing targetPricing = pricingRepository.findByPackageIdAndDurationMonths(pkg.getPackageId(), d.getDurationMonths())
                        .orElse(null);
                
                if (targetPricing == null) continue;

                BigDecimal targetEffectivePrice = targetPricing.getDiscountPrice() != null ? targetPricing.getDiscountPrice() : targetPricing.getBasePrice();

                boolean isTierUpgrade = !isSamePackage && targetEffectivePrice.compareTo(currentEffectivePrice) > 0;
                
                // If specific targetDurationMonths requested, filter by it
                if (targetDurationMonths != null && !d.getDurationMonths().equals(targetDurationMonths)) {
                    continue;
                }

                if (isDurationUpgrade || isTierUpgrade) {
                    BigDecimal payableDiff = targetEffectivePrice.subtract(currentEffectivePrice);
                    if (payableDiff.compareTo(BigDecimal.ZERO) <= 0) continue; // Must be positive

                    // Calculate limits
                    PackageVisitLimit targetLimit = visitLimitRepository.findByPackageIdAndDurationMonths(pkg.getPackageId(), d.getDurationMonths())
                            .orElse(null);
                    int targetTotal = targetLimit != null ? targetLimit.getVisitLimit() : 0;
                    int currentRemaining = currentSub.getRemainingLimit() != null ? currentSub.getRemainingLimit() : 0;
                    int newRemaining = Math.max(0, targetTotal - currentRemaining);

                    String badge = null;
                    if (targetPricing.getDiscountPrice() != null && targetPricing.getDiscountPrice().compareTo(targetPricing.getBasePrice()) < 0) {
                        badge = "discount";
                    }

                    options.add(UpgradeOptionDto.builder()
                            .type(isDurationUpgrade ? "duration_upgrade" : "tier_upgrade")
                            .target(TargetPackageDto.builder()
                                    .packageId(pkg.getPackageId())
                                    .packageName(pkg.getName())
                                    .durationMonths(d.getDurationMonths())
                                    .targetTotalLimit(targetTotal)
                                    .targetEffectivePrice(targetEffectivePrice)
                                    .build())
                            .payableDifference(payableDiff)
                            .newRemainingLimit(newRemaining)
                            .badge(badge)
                            .build());
                }
            }
        }

        return UpgradeOptionsResponse.builder()
                .current(currentDetails)
                .options(options)
                .build();
    }

    @Transactional
    public UpgradeCheckoutResponse checkout(Long userId, UpgradeCheckoutRequest request) {
        // Validate Current Subscription
        Subscription currentSub = subscriptionRepository.findById(request.getCurrentSubscriptionId())
                .orElseThrow(() -> new ServiceException("Active subscription not found", org.springframework.http.HttpStatus.CONFLICT, "NO_ACTIVE_SUBSCRIPTION"));
        
        if (!currentSub.getUserId().equals(userId)) {
             throw new ServiceException("Unauthorized access to subscription", org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
        if (!"ACTIVE".equals(currentSub.getStatus())) {
            throw new ServiceException("Subscription is not active", org.springframework.http.HttpStatus.CONFLICT, "NO_ACTIVE_SUBSCRIPTION");
        }

        // Validate Target
        SubscriptionPackage targetPkg = packageRepository.findById(request.getTargetPackageId())
                .orElseThrow(() -> new ServiceException("Target package not found", org.springframework.http.HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND"));
        
        if (!targetPkg.getIsActive()) throw new ServiceException("Target package is not active", org.springframework.http.HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND");
        
        PackagePricing targetPricing = pricingRepository.findByPackageIdAndDurationMonths(request.getTargetPackageId(), request.getTargetDurationMonths())
                .orElseThrow(() -> new ServiceException("Invalid upgrade request configuration", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_UPGRADE_REQUEST"));
        
        // Validate Eligibility & Calculate Price (Similar logic to getUpgradeOptions)
        // Infer current duration
        long currentDurationLong = 1;
        if (currentSub.getEndAt() != null) {
            currentDurationLong = java.time.temporal.ChronoUnit.MONTHS.between(currentSub.getStartAt(), currentSub.getEndAt());
            if (currentDurationLong == 0) currentDurationLong = 1;
        }
        int currentDuration = (int) currentDurationLong;

        PackagePricing currentPricing = pricingRepository.findByPackageIdAndDurationMonths(currentSub.getPackageId(), currentDuration)
                .orElseThrow(() -> new ServiceException("Current package configuration not found", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"));
        BigDecimal currentEffectivePrice = currentPricing.getDiscountPrice() != null ? currentPricing.getDiscountPrice() : currentPricing.getBasePrice();
        BigDecimal targetEffectivePrice = targetPricing.getDiscountPrice() != null ? targetPricing.getDiscountPrice() : targetPricing.getBasePrice();

        BigDecimal payableDiff = targetEffectivePrice.subtract(currentEffectivePrice);
        
        // Validation: payable_difference > 0
        if (payableDiff.compareTo(BigDecimal.ZERO) <= 0) {
             throw new ServiceException("Upgrade not eligible (price difference <= 0)", org.springframework.http.HttpStatus.CONFLICT, "UPGRADE_NOT_ELIGIBLE");
        }
        
        // Create Order ID
        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8); // simplified

        // Process Payment
        PaymentResultDto paymentResult = paymentService.processPayment(request.getPaymentMethodId(), payableDiff, targetPricing.getCurrency());

        // Save Order
        Order order = Order.builder()
                .orderId(orderId)
                .userId(userId)
                .type("subscription_upgrade")
                .status(paymentResult.getStatus())
                .amount(payableDiff)
                .currency(targetPricing.getCurrency())
                .createdAt(LocalDateTime.now())
                .providerReference(paymentResult.getProviderReference())
                .build();
        orderRepository.save(order);

        SubscriptionDetailsDto subDetails = null;

        if ("success".equals(paymentResult.getStatus())) {
            // Update Subscription
            PackageVisitLimit targetLimit = visitLimitRepository.findByPackageIdAndDurationMonths(request.getTargetPackageId(), request.getTargetDurationMonths())
                            .orElse(null);
            int targetTotal = targetLimit != null ? targetLimit.getVisitLimit() : 0;
            int currentRemaining = currentSub.getRemainingLimit() != null ? currentSub.getRemainingLimit() : 0;
            int newRemaining = Math.max(0, targetTotal - currentRemaining);

            currentSub.setPackageId(request.getTargetPackageId());
            currentSub.setTotalLimit(targetTotal);
            currentSub.setRemainingLimit(newRemaining);
            
            // Recalculate End Date. EndAt = StartAt + TargetDuration
            currentSub.setEndAt(currentSub.getStartAt().plusMonths(request.getTargetDurationMonths()));
            
            subscriptionRepository.save(currentSub);
            
            subDetails = SubscriptionDetailsDto.builder()
                .subscriptionId(currentSub.getSubscriptionId())
                .packageId(currentSub.getPackageId())
                .packageName(targetPkg.getName())
                .durationMonths(request.getTargetDurationMonths())
                .totalLimit(targetTotal)
                .remainingLimit(newRemaining)
                .startAt(currentSub.getStartAt())
                .endAt(currentSub.getEndAt())
                .build();
        }

        return UpgradeCheckoutResponse.builder()
                .orderId(orderId)
                .payment(paymentResult)
                .subscription(subDetails)
                .subscriptionUnchanged(!"success".equals(paymentResult.getStatus()))
                .build();
    }
    
    public Order getOrder(Long userId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException("Order not found", org.springframework.http.HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        if (!order.getUserId().equals(userId)) {
             throw new ServiceException("Order not found", org.springframework.http.HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND");
        }
        return order;
    }
}
