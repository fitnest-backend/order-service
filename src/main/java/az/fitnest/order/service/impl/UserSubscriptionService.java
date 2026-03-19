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
import az.fitnest.order.util.UserContext;
import az.fitnest.order.event.SubscriptionEventPublisher;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPackageRepository packageRepository;
    private final az.fitnest.order.repository.GymVisitRepository gymVisitRepository;
    private final az.fitnest.order.repository.OrderRepository orderRepository;
    private final SubscriptionEventPublisher subscriptionEventPublisher;

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
            subscriptionEventPublisher.publishSubscriptionEvent(userId, "CHECKIN", subscription.getSubscriptionId());
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
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserSubscriptionService.class);
        try {
            log.info("Fetching active subscription for userId={}", userId);
            Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                    .orElse(null);

            String subscriptionStatus = null;

            if (subscription == null) {
                log.info("No ACTIVE subscription found for userId={}, checking FROZEN", userId);
                subscription = subscriptionRepository.findByUserIdAndStatus(userId, "FROZEN")
                        .orElse(null);
                if (subscription != null) {
                    subscriptionStatus = "frozen";
                    log.info("Found FROZEN subscription for userId={}, subscriptionId={}", userId, subscription.getSubscriptionId());
                }
            } else {
                subscriptionStatus = "active";
                log.info("Found ACTIVE subscription for userId={}, subscriptionId={}", userId, subscription.getSubscriptionId());
            }

            if (subscription == null) {
                log.info("No subscription found for userId={}, returning No Plan", userId);
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

            if (subscription.getEndAt() != null && subscription.getEndAt().isBefore(java.time.LocalDateTime.now())) {
                log.info("Subscription expired for userId={}, subscriptionId={}", userId, subscription.getSubscriptionId());
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

            log.info("Fetching package for subscriptionId={}, packageId={}", subscription.getSubscriptionId(), subscription.getPackageId());
            SubscriptionPackage pkg = packageRepository.findById(subscription.getPackageId())
                    .orElse(null);
            if (pkg == null) {
                log.error("Package not found for packageId={} (userId={})", subscription.getPackageId(), userId);
                throw new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found");
            }

            long durationMonths = 1;
            if (subscription.getEndAt() != null) {
                durationMonths = java.time.temporal.ChronoUnit.MONTHS.between(subscription.getStartAt(), subscription.getEndAt());
                if (durationMonths == 0) durationMonths = 1;
            }
            Integer duration = (int) durationMonths;

            log.info("Looking for package option with duration={} months in packageId={}", duration, pkg.getId());
            java.math.BigDecimal effectivePrice = java.math.BigDecimal.ZERO;
            PackageOption matchedOption = pkg.getOptions() != null ? pkg.getOptions().stream()
                    .filter(o -> o.getDurationMonths().equals(duration))
                    .findFirst()
                    .orElse(null) : null;
            if (matchedOption == null) {
                log.warn("No matching package option found for duration={} months in packageId={} (userId={})", duration, pkg.getId(), userId);
            }

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

            log.info("Building subscription details DTO for userId={}, subscriptionId={}", userId, subscription.getSubscriptionId());
            java.util.List<az.fitnest.order.dto.PackageBenefitDto> benefitDtos = java.util.Collections.emptyList();
            Long optionId = null;
            if (matchedOption != null) {
                optionId = matchedOption.getId();
                if (matchedOption.getBenefits() != null) {
                    benefitDtos = matchedOption.getBenefits().stream()
                        .map(b -> az.fitnest.order.dto.PackageBenefitDto.builder()
                            .logo(b.getLogo())
                            .description(b.getDescription())
                            .build())
                        .toList();
                }
            }
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
                    .frozenAt(subscription.getFrozenAt() != null ? subscription.getFrozenAt().toLocalDate() : null)
                    .unfreezesAt(subscription.getUnfreezesAt() != null ? subscription.getUnfreezesAt().toLocalDate() : null)
                    .frozenDaysUsed(frozenDaysUsed)
                    .allowedFreezeDays(allowedFreezeDays)
                    .remainingFreezeDays(Math.max(0, remainingFreezeDays))
                    .optionId(optionId)
                    .benefits(benefitDtos)
                    .build();

            log.info("Returning subscription details for userId={}, subscriptionId={}", userId, subscription.getSubscriptionId());
            return ActiveSubscriptionResponse.builder()
                    .status(subscriptionStatus)
                    .subscription(details)
                    .build();
        } catch (Exception ex) {
            log.error("Exception in getActiveSubscription for userId={}: {}", userId, ex.getMessage(), ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    @Transactional
    public void freezeSubscription(Long userId) {
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

        int daysToFreeze = availableFreezeDays;

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

        subscriptionEventPublisher.publishSubscriptionEvent(userId, "FREEZE", subscription.getSubscriptionId());
    }

    @Transactional
    public void unfreezeSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "FROZEN")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_frozen_subscription"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime frozenAt = subscription.getFrozenAt();
        LocalDateTime unfreezesAt = subscription.getUnfreezesAt();

        if (frozenAt == null || unfreezesAt == null) {
            subscription.setStatus("ACTIVE");
            subscription.setFrozenAt(null);
            subscription.setUnfreezesAt(null);
            subscriptionRepository.save(subscription);
            return;
        }

        long hoursPassed = java.time.temporal.ChronoUnit.HOURS.between(frozenAt, now);
        int actualDaysUsed = (int) (hoursPassed / 24) + 1;

        long daysOriginallyFrozen = java.time.temporal.ChronoUnit.DAYS.between(frozenAt.toLocalDate(), unfreezesAt.toLocalDate());

        int daysToRefund = (int) daysOriginallyFrozen - actualDaysUsed;

        if (daysToRefund > 0) {
            if (subscription.getEndAt() != null) {
                subscription.setEndAt(subscription.getEndAt().minusDays(daysToRefund));
            }
            subscription.setFrozenDaysUsed(Math.max(0, subscription.getFrozenDaysUsed() - daysToRefund));
        }

        subscription.setStatus("ACTIVE");
        subscription.setFrozenAt(null);
        subscription.setUnfreezesAt(null);

        subscriptionRepository.save(subscription);

        subscriptionEventPublisher.publishSubscriptionEvent(userId, "UNFREEZE", subscription.getSubscriptionId());

        log.info("Manually unfroze subscription {} for user {}. Refunded {} days.",
                subscription.getSubscriptionId(), userId, Math.max(0, daysToRefund));
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

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoFinishExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<String> statuses = List.of("ACTIVE", "FROZEN");
        List<Subscription> expiredSubs = subscriptionRepository.findByStatusInAndEndAtBefore(statuses, now);
        for (Subscription subscription : expiredSubs) {
            subscription.setStatus("EXPIRED");
            subscriptionRepository.save(subscription);
            log.info("Auto-expired subscription {} for user {} (endAt={})", subscription.getSubscriptionId(), subscription.getUserId(), subscription.getEndAt());
        }
        if (!expiredSubs.isEmpty()) {
            log.info("Auto-expired {} subscriptions.", expiredSubs.size());
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
                    subscriptionEventPublisher.publishSubscriptionEvent(request.userId(), "CANCELLED", existing.getSubscriptionId());
                });

        subscriptionRepository.findByUserIdAndStatus(request.userId(), "FROZEN")
                .ifPresent(existing -> {
                    existing.setStatus("CANCELLED");
                    existing.setFrozenAt(null);
                    existing.setUnfreezesAt(null);
                    subscriptionRepository.save(existing);
                    log.info("Admin cancelled existing FROZEN subscription {} for user {}",
                            existing.getSubscriptionId(), request.userId());
                    subscriptionEventPublisher.publishSubscriptionEvent(request.userId(), "CANCELLED", existing.getSubscriptionId());
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
        subscriptionEventPublisher.publishSubscriptionEvent(request.userId(), "ASSIGNED", saved.getSubscriptionId());

        return az.fitnest.order.dto.AdminAssignSubscriptionResponse.builder()
                .subscriptionId(saved.getSubscriptionId())
                .userId(saved.getUserId())
                .build();
    }

    @Transactional
    public void revokeSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.no_active_subscription"));

        subscription.setStatus("CANCELLED");
        subscription.setFrozenAt(null);
        subscription.setUnfreezesAt(null);
        subscriptionRepository.save(subscription);
        log.info("Admin revoked subscription {} for user {}", subscription.getSubscriptionId(), userId);
        subscriptionEventPublisher.publishSubscriptionEvent(userId, "REVOKED", subscription.getSubscriptionId());
    }

    public List<Long> getUserIdsByPackageId(Long packageId) {
        return subscriptionRepository.findByPackageId(packageId)
                .stream()
                .map(Subscription::getUserId)
                .toList();
    }

    public List<Long> getUserIdsByDurationMonths(int durationMonths) {
        return subscriptionRepository.findAll().stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()) || "FROZEN".equals(sub.getStatus()))
                .filter(sub -> {
                    if (sub.getStartAt() == null || sub.getEndAt() == null) return false;
                    long months = java.time.temporal.ChronoUnit.MONTHS.between(sub.getStartAt(), sub.getEndAt());
                    if (months == 0) months = 1;
                    return (int) months == durationMonths;
                })
                .map(Subscription::getUserId)
                .toList();
    }

    public List<Long> getUserIdsByType(String type) {
        LocalDateTime now = LocalDateTime.now();
        return switch (type.toLowerCase()) {
            case "all" -> subscriptionRepository.findAll().stream()
                    .map(Subscription::getUserId)
                    .distinct()
                    .toList();
            case "active" -> subscriptionRepository.findByStatusIn(List.of("ACTIVE", "FROZEN")).stream()
                    .map(Subscription::getUserId)
                    .distinct()
                    .toList();
            case "expired" -> subscriptionRepository.findByStatus("EXPIRED").stream()
                    .map(Subscription::getUserId)
                    .distinct()
                    .toList();
            case "upgraded" -> subscriptionRepository.findByIsUpgraded(true).stream()
                    .map(Subscription::getUserId)
                    .distinct()
                    .toList();
            case "last_7_days" -> subscriptionRepository.findByStatusInAndEndAtBetween(
                            List.of("ACTIVE", "FROZEN"), now, now.plusDays(7)).stream()
                    .map(Subscription::getUserId)
                    .distinct()
                    .toList();
            default -> List.of();
        };
    }
}
