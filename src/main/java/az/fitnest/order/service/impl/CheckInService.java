package az.fitnest.order.service.impl;

<<<<<<< HEAD
=======
import az.fitnest.order.client.CatalogServiceGrpcClient;
>>>>>>> 3dda0f5 (fix)
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
<<<<<<< HEAD

    private final SubscriptionRepository subscriptionRepository;
    private final GymVisitRepository gymVisitRepository;
    private final org.springframework.context.MessageSource messageSource;
=======
    private final SubscriptionRepository subscriptionRepository;
    private final GymVisitRepository gymVisitRepository;
    private final org.springframework.context.MessageSource messageSource;
    private final CatalogServiceGrpcClient catalogServiceGrpcClient;
>>>>>>> 3dda0f5 (fix)

    @Transactional
    public CheckInResponse checkIn(Long userId, Long gymId) {
        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        if (subscription == null) {
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.no_active_membership_found", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .build();
        }

<<<<<<< HEAD
=======
        // Validate gym supports user's subscription plan
        boolean gymSupportsPlan = catalogServiceGrpcClient.gymSupportsPlan(gymId, subscription.getPackageId());
        if (!gymSupportsPlan) {
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.subscription_not_valid_for_gym", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .build();
        }

>>>>>>> 3dda0f5 (fix)
        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            subscription.setStatus("EXPIRED");
            subscriptionRepository.save(subscription);
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.membership_expired", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .build();
        }

        if (subscription.getRemainingLimit() != null && subscription.getRemainingLimit() <= 0) {
            return CheckInResponse.builder()
                    .success(false)
                    .message(messageSource.getMessage("error.no_remaining_visits", null, org.springframework.context.i18n.LocaleContextHolder.getLocale()))
                    .remainingVisits(0)
                    .build();
        }

        if (subscription.getRemainingLimit() != null) {
            subscription.setRemainingLimit(subscription.getRemainingLimit() - 1);
            subscriptionRepository.save(subscription);
        }

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
