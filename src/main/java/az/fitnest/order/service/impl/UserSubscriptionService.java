package az.fitnest.order.service.impl;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.dto.SubscriptionDetailsDto;
import az.fitnest.order.model.entity.DurationOption;
import az.fitnest.order.model.entity.MembershipPlan;
import az.fitnest.order.model.entity.Subscription;
import az.fitnest.order.repository.MembershipPlanRepository;
import az.fitnest.order.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
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
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        if (subscription == null) {
            return ActiveSubscriptionResponse.builder()
                    .status("none")
                    .build();
        }

        // Check if expired
        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            return ActiveSubscriptionResponse.builder()
                    .status("none")
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
                .build();

        return ActiveSubscriptionResponse.builder()
                .status("active")
                .subscription(details)
                .build();
    }

    @Transactional
    public void freezeSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_active_subscription_or_frozen"));

        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            throw new az.fitnest.order.exception.BadRequestException("error.membership_expired_cannot_freeze");
        }

        subscription.setStatus("FROZEN");
        subscription.setFrozenAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unfreezeSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "FROZEN")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.frozen_subscription_not_found"));

        if (subscription.getFrozenAt() != null && subscription.getEndAt() != null) {
            long daysFrozen = java.time.temporal.ChronoUnit.DAYS.between(subscription.getFrozenAt(), LocalDateTime.now());
            if (daysFrozen > 0) {
                subscription.setEndAt(subscription.getEndAt().plusDays(daysFrozen));
            }
        }

        subscription.setStatus("ACTIVE");
        subscription.setFrozenAt(null);
        subscriptionRepository.save(subscription);
    }
}
