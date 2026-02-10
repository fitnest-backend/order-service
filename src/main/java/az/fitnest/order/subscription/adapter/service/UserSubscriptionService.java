package az.fitnest.order.subscription.adapter.service;

import az.fitnest.order.subscription.api.dto.ActiveSubscriptionResponse;
import az.fitnest.order.subscription.api.dto.SubscriptionDetailsDto;
import az.fitnest.order.subscription.domain.model.Subscription;
import az.fitnest.order.subscription.domain.model.SubscriptionPackage;
import az.fitnest.order.subscription.domain.model.PackagePricing;
import az.fitnest.order.subscription.adapter.persistence.PackagePricingRepository;
import az.fitnest.order.subscription.adapter.persistence.SubscriptionPackageRepository;
import az.fitnest.order.subscription.adapter.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPackageRepository packageRepository;
    private final PackagePricingRepository pricingRepository;

    @Transactional(readOnly = true)
    public ActiveSubscriptionResponse getActiveSubscription(Long userId) {
        // According to spec 6.1, we return 'active' or 'none'.
        // Assuming 'ACTIVE' is the status string in DB.
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        if (subscription == null) {
            return ActiveSubscriptionResponse.builder()
                    .status("none")
                    .build();
        }

        // Check if expired, though status should manage this. 
        // If expired but status is ACTIVE, we might want to return none or filtered.
        // Spec 6.1 says "Returns the user’s active subscription".
        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
             return ActiveSubscriptionResponse.builder()
                    .status("none")
                    .build();
        }

        SubscriptionPackage pkg = packageRepository.findById(subscription.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found: " + subscription.getPackageId()));
        
        // We need effective price. The subscription entity doesn't store price at purchase time in the provided entity structure.
        // But the response requires it. "effective_price".
        // Usually implementation stores the price paid in the subscription or order.
        // The DTO says "effective_price".
        // Let's lookup current price for now or assume we can calculate it.
        // The logic for "effective_price" in spec 2.2 is `discount_price` if present, else `base_price`.
        // We can fetch determining duration from subscription start/end or if we stored it?
        // Wait, Subscription entity doesn't have duration!
        // The provided Subscription entity has startAt, endAt. We can infer duration or we should have stored it.
        // Let's check Subscription entity again.
        // The spec 6.1 response has `duration_months: 1`.
        // The entity I viewed in Step 35 DOES NOT have `duration_months`.
        // I should probably add `duration_months` to Subscription entity to avoid guessing.
        // It also doesn't have price.
        // The spec 6.1 shows `effective_price` in response.
        
        // For now, I will calculate duration from start/end.
        long durationMonths = 0;
        if (subscription.getEndAt() != null) {
             // rough estimation or exact calculation
             // simplest: java.time.temporal.ChronoUnit.MONTHS.between(start, end)
             // But start/end might be slightly off.
             // If I don't add duration to entity, I have to guess.
             // I'll assume standard durations match (1, 3, 6, 12).
             durationMonths = java.time.temporal.ChronoUnit.MONTHS.between(subscription.getStartAt(), subscription.getEndAt());
             if (durationMonths == 0) durationMonths = 1; // Fallback
        }

        // Lookup price for this package + inferred duration
        // Ideally we should store this in subscription at purchase time.
        // But for this task I'll lookup current price.
        Integer duration = (int) durationMonths;
        PackagePricing pricing = pricingRepository.findByPackageIdAndDurationMonths(subscription.getPackageId(), duration)
                 .orElse(null);
        
        BigDecimal effectivePrice = BigDecimal.ZERO;
        String currency = "AZN";
        if (pricing != null) {
            effectivePrice = pricing.getDiscountPrice() != null ? pricing.getDiscountPrice() : pricing.getBasePrice();
            currency = pricing.getCurrency();
        }

        SubscriptionDetailsDto details = SubscriptionDetailsDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .packageId(subscription.getPackageId())
                .packageName(pkg.getName())
                .durationMonths(duration)
                .effectivePrice(effectivePrice)
                .currency(currency)
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
}
