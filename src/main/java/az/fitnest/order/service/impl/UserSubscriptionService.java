package az.fitnest.order.service.impl;

import az.fitnest.order.dto.ActiveSubscriptionResponse;
import az.fitnest.order.dto.SubscriptionDetailsDto;
import az.fitnest.order.model.entity.PackageOption;
import az.fitnest.order.model.entity.SubscriptionPackage;
import az.fitnest.order.model.entity.Subscription;
import az.fitnest.order.repository.SubscriptionPackageRepository;
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
    private final SubscriptionPackageRepository packageRepository;
    private final az.fitnest.order.repository.GymVisitRepository gymVisitRepository;

    @Transactional
    public boolean checkIn(Long userId, Long gymId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_active_subscription"));

        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
            throw new az.fitnest.order.exception.BadRequestException("error.membership_expired");
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

        String subscriptionStatus = null;

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

        if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(LocalDateTime.now())) {
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

        SubscriptionPackage pkg = packageRepository.findById(subscription.getPackageId())
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        long durationMonths = 1;
        if (subscription.getEndAt() != null) {
            durationMonths = java.time.temporal.ChronoUnit.MONTHS.between(subscription.getStartAt(), subscription.getEndAt());
            if (durationMonths == 0) durationMonths = 1;
        }
        Integer duration = (int) durationMonths;

        BigDecimal effectivePrice = BigDecimal.ZERO;
        PackageOption matchedOption = pkg.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(duration))
                .findFirst()
                .orElse(null);

        if (matchedOption != null) {
            effectivePrice = matchedOption.getPriceDiscounted() != null
                    ? matchedOption.getPriceDiscounted()
                    : matchedOption.getPriceStandard();
        }

        Integer allowedFreezeDays = matchedOption != null && matchedOption.getFreezeDays() != null
                ? matchedOption.getFreezeDays()
                : 0;
        Integer frozenDaysUsed = subscription.getFrozenDaysUsed() != null ? subscription.getFrozenDaysUsed() : 0;
        Integer remainingFreezeDays = allowedFreezeDays - frozenDaysUsed;

        SubscriptionDetailsDto details = SubscriptionDetailsDto.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .packageId(pkg.getId().toString())
                .packageName(pkg.getName())
                .durationMonths(duration)
                .effectivePrice(effectivePrice)
                .currency(pkg.getCurrency())
                .totalLimit(subscription.getTotalLimit())
                .remainingLimit(subscription.getRemainingLimit())
                .startAt(subscription.getStartAt() != null ? subscription.getStartAt().toLocalDate() : null)
                .endAt(subscription.getEndAt() != null ? subscription.getEndAt().toLocalDate() : null)
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

        SubscriptionPackage pkg = packageRepository.findById(subscription.getPackageId())
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        long tempDurationMonths = 1;
        if (subscription.getEndAt() != null) {
            tempDurationMonths = java.time.temporal.ChronoUnit.MONTHS.between(subscription.getStartAt(), subscription.getEndAt());
            if (tempDurationMonths == 0) tempDurationMonths = 1;
        }
        final long durationMonths = tempDurationMonths;

        PackageOption matchedOption = pkg.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals((int) durationMonths))
                .findFirst()
                .orElse(null);

        Integer allowedFreezeDays = matchedOption != null && matchedOption.getFreezeDays() != null
                ? matchedOption.getFreezeDays()
                : 0;

        if (allowedFreezeDays == 0) {
            throw new az.fitnest.order.exception.BadRequestException("error.freeze_not_allowed_for_plan");
        }

        if (subscription.getFrozenDaysUsed() == null) {
            subscription.setFrozenDaysUsed(0);
        }

        int availableFreezeDays = allowedFreezeDays - subscription.getFrozenDaysUsed();

        if (availableFreezeDays <= 0) {
            throw new az.fitnest.order.exception.BadRequestException("error.freeze_days_exhausted");
        }

        if (daysToFreeze == null || daysToFreeze <= 0) {
            daysToFreeze = availableFreezeDays;
        }

        if (daysToFreeze > availableFreezeDays) {
            throw new az.fitnest.order.exception.BadRequestException(
                String.format("error.freeze_days_exceeded_limit|%d|%d", availableFreezeDays, allowedFreezeDays)
            );
        }

        LocalDateTime unfreezesAt = LocalDateTime.now().plusDays(daysToFreeze);

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

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoUnfreezeExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> expiredFrozenSubs = subscriptionRepository.findAll().stream()
                .filter(sub -> "FROZEN".equals(sub.getStatus()))
                .filter(sub -> sub.getUnfreezesAt() != null)
                .filter(sub -> sub.getUnfreezesAt().isBefore(now) || sub.getUnfreezesAt().isEqual(now))
                .toList();

        for (Subscription subscription : expiredFrozenSubs) {
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

    @Transactional
    public az.fitnest.order.dto.AdminAssignSubscriptionResponse assignSubscriptionToUser(
            az.fitnest.order.dto.AdminAssignSubscriptionRequest request) {

        SubscriptionPackage pkg = packageRepository.findById(request.planId())
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        if (pkg.getIsActive() == null || !pkg.getIsActive()) {
            throw new az.fitnest.order.exception.BadRequestException("error.target_plan_inactive");
        }

        PackageOption option = pkg.getOptions().stream()
                .filter(o -> o.getId().equals(request.optionId()))
                .findFirst()
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.duration_config_not_found"));

        subscriptionRepository.findByUserIdAndStatus(request.userId(), "ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("CANCELLED");
                    subscriptionRepository.save(existing);
                    log.info("Admin cancelled existing ACTIVE subscription {} for user {}",
                            existing.getSubscriptionId(), request.userId());
                });

        subscriptionRepository.findByUserIdAndStatus(request.userId(), "FROZEN")
                .ifPresent(existing -> {
                    existing.setStatus("CANCELLED");
                    existing.setFrozenAt(null);
                    existing.setUnfreezesAt(null);
                    subscriptionRepository.save(existing);
                    log.info("Admin cancelled existing FROZEN subscription {} for user {}",
                            existing.getSubscriptionId(), request.userId());
                });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = now.plusMonths(option.getDurationMonths());

        Integer entryLimit = option.getEntryLimit();
        Integer freezeDays = option.getFreezeDays() != null ? option.getFreezeDays() : 0;

        Subscription subscription = new Subscription();
        subscription.setUserId(request.userId());
        subscription.setPackageId(request.planId());
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now);
        subscription.setEndAt(endAt);
        subscription.setTotalLimit(entryLimit);
        subscription.setRemainingLimit(entryLimit);
        subscription.setFrozenDaysUsed(0);
        subscription.setAllowedFreezeDays(freezeDays);

        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Admin assigned plan {} option {} (duration={} months) to user {}, subscriptionId={}",
                pkg.getName(), option.getId(), option.getDurationMonths(), request.userId(), saved.getSubscriptionId());

        return az.fitnest.order.dto.AdminAssignSubscriptionResponse.builder()
                .subscriptionId(saved.getSubscriptionId())
                .userId(saved.getUserId())
                .planId(saved.getPackageId())
                .planName(pkg.getName())
                .optionId(option.getId())
                .durationMonths(option.getDurationMonths())
                .status(saved.getStatus())
                .startAt(saved.getStartAt())
                .endAt(saved.getEndAt())
                .totalLimit(saved.getTotalLimit())
                .remainingLimit(saved.getRemainingLimit())
                .allowedFreezeDays(freezeDays)
                .message("Abunəlik istifadəçiyə uğurla təyin edildi")
                .build();
    }

    @Transactional
    public void revokeSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .or(() -> subscriptionRepository.findByUserIdAndStatus(userId, "FROZEN"))
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_active_subscription"));

        subscription.setStatus("CANCELLED");
        subscription.setFrozenAt(null);
        subscription.setUnfreezesAt(null);
        subscriptionRepository.save(subscription);

        log.info("Admin revoked subscription {} for user {}", subscription.getSubscriptionId(), userId);
    }
}
