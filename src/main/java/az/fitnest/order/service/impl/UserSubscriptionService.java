package az.fitnest.order.service.impl;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.dto.SubscriptionDetailsDto;
import az.fitnest.order.model.entity.DurationOption;
import az.fitnest.order.model.entity.MembershipPlan;
import az.fitnest.order.model.entity.Subscription;
import az.fitnest.order.repository.MembershipPlanRepository;
import az.fitnest.order.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanRepository planRepository;
    private final az.fitnest.order.repository.GymVisitRepository gymVisitRepository;

    @Transactional
    public boolean checkIn(Long userId, Long gymId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_active_subscription"));

        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            throw new az.fitnest.order.exception.BadRequestException("error.membership_expired");
        }

        if (subscription.getGymId() != null && !subscription.getGymId().equals(gymId)) {
            throw new az.fitnest.order.exception.BadRequestException("error.subscription_not_valid_for_gym");
        }

        if (subscription.getRemainingLimit() != null) {
            if (subscription.getRemainingLimit() <= 0) {
                throw new az.fitnest.order.exception.BadRequestException("error.no_remaining_visits");
            }
            subscription.setRemainingLimit(subscription.getRemainingLimit() - 1);
            subscriptionRepository.save(subscription);
        }

        az.fitnest.order.model.entity.GymVisit visit = az.fitnest.order.model.entity.GymVisit.builder()
                .userId(userId)
                .gymId(gymId)
                .subscriptionId(subscription.getSubscriptionId())
                .checkedInAt(LocalDateTime.now())
                .build();
        gymVisitRepository.save(visit);

        return true;
    }

    @Transactional(readOnly = true)
    public ActiveSubscriptionResponse getActiveSubscription(Long userId) {
        // First check for ACTIVE subscription
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        String subscriptionStatus = null;

        // If no active subscription, check for FROZEN
        if (subscription == null) {
            subscription = subscriptionRepository.findByUserIdAndStatus(userId, "FROZEN")
                    .orElse(null);
            if (subscription != null) {
                subscriptionStatus = "frozen";
            }
        } else {
            subscriptionStatus = "active";
        }

        if (subscription == null) {
            // User has no subscription - return "No Plan"
            SubscriptionDetailsDto noPlanDetails = SubscriptionDetailsDto.builder()
                    .packageName("No Plan")
                    .frozenDaysUsed(0)
                    .allowedFreezeDays(0)
                    .remainingFreezeDays(0)
                    .build();

            return ActiveSubscriptionResponse.builder()
                    .status("none")
                    .subscription(noPlanDetails)
                    .build();
        }

        // Check if expired
        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            // Subscription expired - return "No Plan"
            SubscriptionDetailsDto noPlanDetails = SubscriptionDetailsDto.builder()
                    .packageName("No Plan")
                    .frozenDaysUsed(0)
                    .allowedFreezeDays(0)
                    .remainingFreezeDays(0)
                    .build();

            return ActiveSubscriptionResponse.builder()
                    .status("none")
                    .subscription(noPlanDetails)
                    .build();
        }

        MembershipPlan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        // Infer duration from start/end
        long durationMonths = 1;
        if (subscription.getEndAt() != null) {
            durationMonths = java.time.temporal.ChronoUnit.MONTHS.between(subscription.getStartAt(), subscription.getEndAt());
            if (durationMonths == 0) durationMonths = 1;
        }
        Integer duration = (int) durationMonths;

        // Find matching duration option for effective price
        BigDecimal effectivePrice = BigDecimal.ZERO;
        DurationOption matchedOption = plan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(duration))
                .findFirst()
                .orElse(null);

        if (matchedOption != null) {
            effectivePrice = matchedOption.getPriceDiscounted() != null
                    ? matchedOption.getPriceDiscounted()
                    : matchedOption.getPriceStandard();
        }

        // Calculate freeze days info
        Integer allowedFreezeDays = matchedOption != null && matchedOption.getFreezeDays() != null
                ? matchedOption.getFreezeDays()
                : 0;
        Integer frozenDaysUsed = subscription.getFrozenDaysUsed() != null ? subscription.getFrozenDaysUsed() : 0;
        Integer remainingFreezeDays = allowedFreezeDays - frozenDaysUsed;

        SubscriptionDetailsDto details = SubscriptionDetailsDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .packageId(plan.getId().toString())
                .packageName(plan.getName())
                .durationMonths(duration)
                .effectivePrice(effectivePrice)
                .currency(plan.getCurrency())
                .totalLimit(subscription.getTotalLimit())
                .remainingLimit(subscription.getRemainingLimit())
                .startAt(subscription.getStartAt())
                .endAt(subscription.getEndAt())
                .frozenAt(subscription.getFrozenAt())
                .unfreezesAt(subscription.getUnfreezesAt())
                .frozenDaysUsed(frozenDaysUsed)
                .allowedFreezeDays(allowedFreezeDays)
                .remainingFreezeDays(Math.max(0, remainingFreezeDays))
                .build();

        return ActiveSubscriptionResponse.builder()
                .status(subscriptionStatus)
                .subscription(details)
                .build();
    }

    @Transactional
    public void freezeSubscription(Long userId, Integer daysToFreeze) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_active_subscription"));

        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            throw new az.fitnest.order.exception.BadRequestException("error.membership_expired_cannot_freeze");
        }

        // Get the plan to check allowed freeze days
        MembershipPlan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        // Find matching duration option to get freeze days limit
        long tempDurationMonths = 1;
        if (subscription.getEndAt() != null) {
            tempDurationMonths = java.time.temporal.ChronoUnit.MONTHS.between(subscription.getStartAt(), subscription.getEndAt());
            if (tempDurationMonths == 0) tempDurationMonths = 1;
        }
        final long durationMonths = tempDurationMonths;

        DurationOption matchedOption = plan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals((int) durationMonths))
                .findFirst()
                .orElse(null);

        Integer allowedFreezeDays = matchedOption != null && matchedOption.getFreezeDays() != null
                ? matchedOption.getFreezeDays()
                : 0;

        if (allowedFreezeDays == 0) {
            throw new az.fitnest.order.exception.BadRequestException("error.freeze_not_allowed_for_plan");
        }

        // Initialize frozen days used if null
        if (subscription.getFrozenDaysUsed() == null) {
            subscription.setFrozenDaysUsed(0);
        }

        // Calculate available freeze days
        int availableFreezeDays = allowedFreezeDays - subscription.getFrozenDaysUsed();

        if (availableFreezeDays <= 0) {
            throw new az.fitnest.order.exception.BadRequestException("error.freeze_days_exhausted");
        }

        // If no days specified, freeze for the remaining available days
        if (daysToFreeze == null || daysToFreeze <= 0) {
            daysToFreeze = availableFreezeDays;
        }

        // Validate requested freeze duration
        if (daysToFreeze > availableFreezeDays) {
            throw new az.fitnest.order.exception.BadRequestException(
                String.format("error.freeze_days_exceeded_limit|%d|%d", availableFreezeDays, allowedFreezeDays)
            );
        }

        // Calculate when subscription should auto-unfreeze
        LocalDateTime unfreezesAt = LocalDateTime.now().plusDays(daysToFreeze);

        // Extend end_at by the freeze duration
        if (subscription.getEndAt() != null) {
            subscription.setEndAt(subscription.getEndAt().plusDays(daysToFreeze));
        }

        subscription.setStatus("FROZEN");
        subscription.setFrozenAt(LocalDateTime.now());
        subscription.setUnfreezesAt(unfreezesAt);
        subscription.setFrozenDaysUsed(subscription.getFrozenDaysUsed() + daysToFreeze);
        subscription.setAllowedFreezeDays(allowedFreezeDays);

        subscriptionRepository.save(subscription);
    }

    /**
     * Scheduled task that automatically unfreezes subscriptions when freeze duration expires.
     * Runs every hour.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void autoUnfreezeExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        // Find all frozen subscriptions where unfreeze time has passed
        List<Subscription> expiredFrozenSubs = subscriptionRepository.findAll().stream()
                .filter(sub -> "FROZEN".equals(sub.getStatus()))
                .filter(sub -> sub.getUnfreezesAt() != null)
                .filter(sub -> sub.getUnfreezesAt().isBefore(now) || sub.getUnfreezesAt().isEqual(now))
                .toList();

        for (Subscription subscription : expiredFrozenSubs) {
            // Reactivate the subscription
            subscription.setStatus("ACTIVE");
            subscription.setFrozenAt(null);
            subscription.setUnfreezesAt(null);
            subscriptionRepository.save(subscription);

            log.info("Auto-unfroze subscription {} for user {}",
                    subscription.getSubscriptionId(), subscription.getUserId());
        }

        if (!expiredFrozenSubs.isEmpty()) {
            log.info("Auto-unfroze {} subscriptions", expiredFrozenSubs.size());
        }
    }
}
