package az.fitnest.order.service.impl;

import az.fitnest.order.dto.*;
import az.fitnest.order.entity.*;
import az.fitnest.order.exception.ServiceException;
import az.fitnest.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpgradeService {

    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanRepository planRepository;
    private final MockPaymentService paymentService;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public UpgradeOptionsResponse getUpgradeOptions(Long userId, Integer targetDurationMonths) {
        Subscription currentSub = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ServiceException("User has no active subscription",
                        org.springframework.http.HttpStatus.CONFLICT, "NO_ACTIVE_SUBSCRIPTION"));

        MembershipPlan currentPlan = planRepository.findById(currentSub.getPlanId())
                .orElseThrow(() -> new ServiceException("Current plan not found",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"));

        // Infer current duration
        long currentDurationLong = 1;
        if (currentSub.getEndAt() != null) {
            currentDurationLong = java.time.temporal.ChronoUnit.MONTHS.between(currentSub.getStartAt(), currentSub.getEndAt());
            if (currentDurationLong == 0) currentDurationLong = 1;
        }
        int currentDuration = (int) currentDurationLong;

        // Get current effective price from the matching duration option
        DurationOption currentOption = currentPlan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(currentDuration))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Current plan duration config not found",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"));

        BigDecimal currentEffectivePrice = currentOption.getPriceDiscounted() != null
                ? currentOption.getPriceDiscounted() : currentOption.getPriceStandard();

        // Prepare current details for response
        SubscriptionDetailsDto currentDetails = SubscriptionDetailsDto.builder()
                .subscriptionId(currentSub.getSubscriptionId())
                .packageId(currentPlan.getId().toString())
                .packageName(currentPlan.getName())
                .durationMonths(currentDuration)
                .effectivePrice(currentEffectivePrice)
                .currency(currentPlan.getCurrency())
                .remainingLimit(currentSub.getRemainingLimit())
                .build();

        // Find upgrade candidates
        List<UpgradeOptionDto> options = new ArrayList<>();
        List<MembershipPlan> allPlans = planRepository.findByIsActiveTrue();

        for (MembershipPlan plan : allPlans) {
            for (DurationOption option : plan.getOptions()) {
                boolean isSamePlan = plan.getId().equals(currentPlan.getId());
                boolean isDurationUpgrade = isSamePlan && option.getDurationMonths() > currentDuration;

                BigDecimal targetEffectivePrice = option.getPriceDiscounted() != null
                        ? option.getPriceDiscounted() : option.getPriceStandard();
                if (targetEffectivePrice == null) continue;

                boolean isTierUpgrade = !isSamePlan && targetEffectivePrice.compareTo(currentEffectivePrice) > 0;

                // Filter by requested target duration if specified
                if (targetDurationMonths != null && !option.getDurationMonths().equals(targetDurationMonths)) {
                    continue;
                }

                if (isDurationUpgrade || isTierUpgrade) {
                    BigDecimal payableDiff = targetEffectivePrice.subtract(currentEffectivePrice);
                    if (payableDiff.compareTo(BigDecimal.ZERO) <= 0) continue;

                    int targetTotal = option.getEntryLimit() != null ? option.getEntryLimit() : 0;
                    int currentRemaining = currentSub.getRemainingLimit() != null ? currentSub.getRemainingLimit() : 0;
                    int newRemaining = Math.max(0, targetTotal - currentRemaining);

                    String badge = null;
                    if (option.getPriceDiscounted() != null && option.getPriceStandard() != null
                            && option.getPriceDiscounted().compareTo(option.getPriceStandard()) < 0) {
                        badge = "discount";
                    }

                    options.add(UpgradeOptionDto.builder()
                            .type(isDurationUpgrade ? "duration_upgrade" : "tier_upgrade")
                            .target(TargetPackageDto.builder()
                                    .packageId(plan.getId().toString())
                                    .packageName(plan.getName())
                                    .durationMonths(option.getDurationMonths())
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
                .orElseThrow(() -> new ServiceException("Active subscription not found",
                        org.springframework.http.HttpStatus.CONFLICT, "NO_ACTIVE_SUBSCRIPTION"));

        if (!currentSub.getUserId().equals(userId)) {
            throw new ServiceException("Unauthorized access to subscription",
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
        if (!"ACTIVE".equals(currentSub.getStatus())) {
            throw new ServiceException("Subscription is not active",
                    org.springframework.http.HttpStatus.CONFLICT, "NO_ACTIVE_SUBSCRIPTION");
        }

        // Validate Target Plan
        Long targetPlanId = Long.parseLong(request.getTargetPackageId());
        MembershipPlan targetPlan = planRepository.findById(targetPlanId)
                .orElseThrow(() -> new ServiceException("Target plan not found",
                        org.springframework.http.HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND"));

        if (!targetPlan.getIsActive()) {
            throw new ServiceException("Target plan is not active",
                    org.springframework.http.HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND");
        }

        DurationOption targetOption = targetPlan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(request.getTargetDurationMonths()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Invalid upgrade request configuration",
                        org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_UPGRADE_REQUEST"));

        // Current plan pricing
        MembershipPlan currentPlan = planRepository.findById(currentSub.getPlanId())
                .orElseThrow(() -> new ServiceException("Current plan not found",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"));

        long currentDurationLong = 1;
        if (currentSub.getEndAt() != null) {
            currentDurationLong = java.time.temporal.ChronoUnit.MONTHS.between(currentSub.getStartAt(), currentSub.getEndAt());
            if (currentDurationLong == 0) currentDurationLong = 1;
        }
        int currentDuration = (int) currentDurationLong;

        DurationOption currentOption = currentPlan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(currentDuration))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Current plan config not found",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"));

        BigDecimal currentEffectivePrice = currentOption.getPriceDiscounted() != null
                ? currentOption.getPriceDiscounted() : currentOption.getPriceStandard();
        BigDecimal targetEffectivePrice = targetOption.getPriceDiscounted() != null
                ? targetOption.getPriceDiscounted() : targetOption.getPriceStandard();

        BigDecimal payableDiff = targetEffectivePrice.subtract(currentEffectivePrice);

        if (payableDiff.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Upgrade not eligible (price difference <= 0)",
                    org.springframework.http.HttpStatus.CONFLICT, "UPGRADE_NOT_ELIGIBLE");
        }

        // Create Order
        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);

        PaymentResultDto paymentResult = paymentService.processPayment(
                request.getPaymentMethodId(), payableDiff, targetPlan.getCurrency());

        Order order = Order.builder()
                .orderId(orderId)
                .userId(userId)
                .type("subscription_upgrade")
                .status(paymentResult.getStatus())
                .amount(payableDiff)
                .currency(targetPlan.getCurrency())
                .createdAt(LocalDateTime.now())
                .providerReference(paymentResult.getProviderReference())
                .build();
        orderRepository.save(order);

        SubscriptionDetailsDto subDetails = null;

        if ("success".equals(paymentResult.getStatus())) {
            int targetTotal = targetOption.getEntryLimit() != null ? targetOption.getEntryLimit() : 0;
            int currentRemaining = currentSub.getRemainingLimit() != null ? currentSub.getRemainingLimit() : 0;
            int newRemaining = Math.max(0, targetTotal - currentRemaining);

            currentSub.setPlanId(targetPlanId);
            currentSub.setTotalLimit(targetTotal);
            currentSub.setRemainingLimit(newRemaining);
            currentSub.setEndAt(currentSub.getStartAt().plusMonths(request.getTargetDurationMonths()));

            subscriptionRepository.save(currentSub);

            subDetails = SubscriptionDetailsDto.builder()
                    .subscriptionId(currentSub.getSubscriptionId())
                    .packageId(targetPlan.getId().toString())
                    .packageName(targetPlan.getName())
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
                .orElseThrow(() -> new ServiceException("Order not found",
                        org.springframework.http.HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND"));
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("Order not found",
                    org.springframework.http.HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND");
        }
        return order;
    }
}
