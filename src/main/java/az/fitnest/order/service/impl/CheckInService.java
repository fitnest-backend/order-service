package az.fitnest.order.service.impl;

import az.fitnest.order.dto.CheckInResponse;
import az.fitnest.order.model.entity.GymVisit;
import az.fitnest.order.model.entity.Subscription;
import az.fitnest.order.repository.GymVisitRepository;
import az.fitnest.order.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final SubscriptionRepository subscriptionRepository;
    private final GymVisitRepository gymVisitRepository;

    @Transactional
    public CheckInResponse checkIn(Long userId, Long gymId) {
        // 1. Find active subscription for this user + gym
        Subscription subscription = subscriptionRepository
                .findByUserIdAndGymIdAndStatus(userId, gymId, "ACTIVE")
                .orElse(null);

        if (subscription == null) {
            return CheckInResponse.builder()
                    .success(false)
                    .message("No active membership found for this gym")
                    .build();
        }

        // 2. Check subscription hasn't expired
        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            subscription.setStatus("EXPIRED");
            subscriptionRepository.save(subscription);
            return CheckInResponse.builder()
                    .success(false)
                    .message("Your membership has expired")
                    .build();
        }

        // 3. Check remaining visits (null means unlimited)
        if (subscription.getRemainingLimit() != null && subscription.getRemainingLimit() <= 0) {
            return CheckInResponse.builder()
                    .success(false)
                    .message("You have no remaining visits on your membership")
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

        log.info("User {} checked in at gym {} (subscription {}). Remaining visits: {}",
                userId, gymId, subscription.getSubscriptionId(), subscription.getRemainingLimit());

        return CheckInResponse.builder()
                .success(true)
                .message("Check-in successful")
                .remainingVisits(subscription.getRemainingLimit())
                .checkedInAt(now)
                .build();
    }
}
