package az.fitnest.order.service.impl;

import az.fitnest.order.dto.CheckInResponse;
import az.fitnest.order.model.entity.GymVisit;
import az.fitnest.order.model.entity.Subscription;
import az.fitnest.order.repository.GymVisitRepository;
import az.fitnest.order.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final SubscriptionRepository subscriptionRepository;
    private final GymVisitRepository gymVisitRepository;
    private final org.springframework.context.MessageSource messageSource;

    @Transactional
    public CheckInResponse checkIn(Long userId, Long gymId) {
        // 1. Find active subscription for this user + gym
        Subscription subscription = subscriptionRepository
                .findByUserIdAndGymIdAndStatus(userId, gymId, "ACTIVE")
                .orElse(null);

        if (subscription == null) {
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.no_active_membership_found", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .build();
        }

        // 2. Check subscription hasn't expired
        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            subscription.setStatus("EXPIRED");
            subscriptionRepository.save(subscription);
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.membership_expired", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .build();
        }

        // 3. Check remaining visits (null means unlimited)
        if (subscription.getRemainingLimit() != null && subscription.getRemainingLimit() <= 0) {
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.no_remaining_visits", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .remainingVisits(0)
                    .build();
        }

        // 4. Decrement visit counter
        if (subscription.getRemainingLimit() != null) {
            subscription.setRemainingLimit(subscription.getRemainingLimit() - 1);
            subscriptionRepository.save(subscription);
        }

        // 5. Record the visit
        LocalDateTime now = LocalDateTime.now();
        GymVisit visit = GymVisit.builder()
                .userId(userId)
                .gymId(gymId)
                .subscriptionId(subscription.getSubscriptionId())
                .checkedInAt(now)
                .build();
        gymVisitRepository.save(visit);


        return CheckInResponse.builder()
                .success(true)
                .message(messageSource.getMessage("error.check_in_successful", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                .remainingVisits(subscription.getRemainingLimit())
                .checkedInAt(now)
                .build();
    }
}
