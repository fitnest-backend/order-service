package az.fitnest.order.service.impl;

import az.fitnest.order.dto.*;
import az.fitnest.order.model.entity.*;
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
                .orElseThrow(() -> new ServiceException("error.no_active_subscription",
                        "NO_ACTIVE_SUBSCRIPTION", org.springframework.http.HttpStatus.CONFLICT));

        MembershipPlan currentPlan = planRepository.findById(currentSub.getPlanId())
                .orElseThrow(() -> new ServiceException("error.plan_not_found",
                        "INTERNAL_ERROR", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

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
                .orElseThrow(() -> new ServiceException("error.duration_config_not_found",
                        "INTERNAL_ERROR", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

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

                int currentTier = getTierRank(currentPlan.getName());
                int targetTier = getTierRank(plan.getName());
                boolean isTierUpgrade = !isSamePlan && targetTier > currentTier;

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
                                    .optionId(option.getId())
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
        Subscription currentSub = subscriptionRepository.findById(request.currentSubscriptionId())
                .orElseThrow(() -> new ServiceException("error.no_active_subscription",
                        "NO_ACTIVE_SUBSCRIPTION", org.springframework.http.HttpStatus.CONFLICT));

        if (!currentSub.getUserId().equals(userId)) {
            throw new ServiceException("error.upgrade_unauthorized",
                    "UNAUTHORIZED", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        if (!"ACTIVE".equals(currentSub.getStatus())) {
            throw new ServiceException("error.no_active_subscription",
                    "NO_ACTIVE_SUBSCRIPTION", org.springframework.http.HttpStatus.CONFLICT);
        }

        // Validate Target Plan
        Long targetPlanId = Long.parseLong(request.targetPackageId());
        MembershipPlan targetPlan = planRepository.findById(targetPlanId)
                .orElseThrow(() -> new ServiceException("error.target_plan_not_found",
                        "PACKAGE_NOT_FOUND", org.springframework.http.HttpStatus.NOT_FOUND));

        if (!targetPlan.getIsActive()) {
            throw new ServiceException("error.target_plan_inactive",
                    "PACKAGE_NOT_FOUND", org.springframework.http.HttpStatus.NOT_FOUND);
        }

        DurationOption targetOption = targetPlan.getOptions().stream()
                .filter(o -> o.getId().equals(request.targetOptionId()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("error.invalid_upgrade_request",
                        "INVALID_UPGRADE_REQUEST", org.springframework.http.HttpStatus.BAD_REQUEST));

        // Current plan pricing
        MembershipPlan currentPlan = planRepository.findById(currentSub.getPlanId())
                .orElseThrow(() -> new ServiceException("error.plan_not_found",
                        "INTERNAL_ERROR", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        long currentDurationLong = 1;
        if (currentSub.getEndAt() != null) {
            currentDurationLong = java.time.temporal.ChronoUnit.MONTHS.between(currentSub.getStartAt(), currentSub.getEndAt());
            if (currentDurationLong == 0) currentDurationLong = 1;
        }
        int currentDuration = (int) currentDurationLong;

        DurationOption currentOption = currentPlan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(currentDuration))
                .findFirst()
                .orElseThrow(() -> new ServiceException("error.duration_config_not_found",
                        "INTERNAL_ERROR", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        BigDecimal currentEffectivePrice = currentOption.getPriceDiscounted() != null
                ? currentOption.getPriceDiscounted() : currentOption.getPriceStandard();
        BigDecimal targetEffectivePrice = targetOption.getPriceDiscounted() != null
                ? targetOption.getPriceDiscounted() : targetOption.getPriceStandard();

        BigDecimal payableDiff = targetEffectivePrice.subtract(currentEffectivePrice);

        if (payableDiff.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("error.upgrade_not_eligible",
                    "UPGRADE_NOT_ELIGIBLE", org.springframework.http.HttpStatus.CONFLICT);
        }

        // Create Order
        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);

        PaymentResultDto paymentResult = paymentService.processPayment(
                request.paymentMethodId(), payableDiff, targetPlan.getCurrency());

        Order order = Order.builder()
                .orderId(orderId)
                .userId(userId)
                .type("subscription_upgrade")
                .status(paymentResult.status())
                .amount(payableDiff)
                .currency(targetPlan.getCurrency())
                .createdAt(LocalDateTime.now())
                .providerReference(paymentResult.providerReference())
                .build();
        orderRepository.save(order);

        SubscriptionDetailsDto subDetails = null;

        if ("success".equals(paymentResult.status())) {
            int targetTotal = targetOption.getEntryLimit() != null ? targetOption.getEntryLimit() : 0;
            int currentRemaining = currentSub.getRemainingLimit() != null ? currentSub.getRemainingLimit() : 0;
            int newRemaining = Math.max(0, targetTotal - currentRemaining);

            currentSub.setPlanId(targetPlanId);
            currentSub.setTotalLimit(targetTotal);
            currentSub.setRemainingLimit(newRemaining);
            currentSub.setEndAt(currentSub.getStartAt().plusMonths(targetOption.getDurationMonths()));

            subscriptionRepository.save(currentSub);

            subDetails = SubscriptionDetailsDto.builder()
                    .subscriptionId(currentSub.getSubscriptionId())
                    .packageId(targetPlan.getId().toString())
                    .packageName(targetPlan.getName())
                    .durationMonths(targetOption.getDurationMonths())
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
                .subscriptionUnchanged(!"success".equals(paymentResult.status()))
                .build();
    }

    public Order getOrder(Long userId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException("error.order_not_found",
                        "ORDER_NOT_FOUND", org.springframework.http.HttpStatus.NOT_FOUND));
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("error.order_not_found",
                    "ORDER_NOT_FOUND", org.springframework.http.HttpStatus.NOT_FOUND);
        }
        return order;
    }

    private int getTierRank(String planName) {
        if (planName == null) return 0;
        String lower = planName.toLowerCase();
        if (lower.contains("platinum")) return 4;
        if (lower.contains("gold")) return 3;
        if (lower.contains("silver")) return 2;
        if (lower.contains("bronze")) return 1;
        return 0;
    }
}
