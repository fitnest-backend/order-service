package az.fitnest.order.config;

import az.fitnest.order.model.entity.*;
import az.fitnest.order.model.enums.BillingPeriod;
import az.fitnest.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final SubscriptionPackageRepository packageRepository;
    private final TranslationRepository translationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Bean
    public CommandLineRunner initOrderData() {
        return args -> {
            logger.info("Running DataInitializer for order-service startup...");
            try {
                initSubscriptionPackages();
                initExpiredSubscriptionForUser1();
                logger.info("DataInitializer completed successfully.");
            } catch (Exception e) {
                logger.error("DataInitializer failed: {}", e.getMessage(), e);
            }
        };
    }

    private void initSubscriptionPackages() {
        if (packageRepository.count() == 0) {

            List<PlanBenefit> bronzeBenefits = Arrays.asList(
                    new PlanBenefit("Access to 5 gyms"),
                    new PlanBenefit("Email support")
            );
            List<String> bronzeServices = Arrays.asList("GYM");

            createSubscriptionPackage("Bronze", new BigDecimal("19.99"), 1, 1,  10,  2, bronzeBenefits, bronzeServices);
            createSubscriptionPackage("Bronze", new BigDecimal("54.99"), 3, 1,  30,  4, bronzeBenefits, bronzeServices);
            createSubscriptionPackage("Bronze", new BigDecimal("99.99"), 6, 1,  60,  7, bronzeBenefits, bronzeServices);
            createSubscriptionPackage("Bronze", new BigDecimal("179.99"), 12, 1, 120, 14, bronzeBenefits, bronzeServices);

            List<PlanBenefit> silverBenefits = Arrays.asList(
                    new PlanBenefit("Access to 15 gyms"),
                    new PlanBenefit("Priority email support"),
                    new PlanBenefit("Sauna access")
            );
            List<String> silverServices = Arrays.asList("GYM", "SAUNA");

            createSubscriptionPackage("Silver", new BigDecimal("39.99"), 1, 2,  20,  4, silverBenefits, silverServices);
            createSubscriptionPackage("Silver", new BigDecimal("109.99"), 3, 2,  60,  8, silverBenefits, silverServices);
            createSubscriptionPackage("Silver", new BigDecimal("199.99"), 6, 2, 120, 14, silverBenefits, silverServices);
            createSubscriptionPackage("Silver", new BigDecimal("359.99"), 12, 2, 240, 28, silverBenefits, silverServices);

            List<PlanBenefit> goldBenefits = Arrays.asList(
                    new PlanBenefit("Access to all gyms"),
                    new PlanBenefit("24/7 support"),
                    new PlanBenefit("Pool access"),
                    new PlanBenefit("1 monthly personal trainer session")
            );
            List<String> goldServices = Arrays.asList("GYM", "SAUNA", "POOL");

            createSubscriptionPackage("Gold", new BigDecimal("69.99"), 1, 3,  30,  6, goldBenefits, goldServices);
            createSubscriptionPackage("Gold", new BigDecimal("189.99"), 3, 3,  90, 12, goldBenefits, goldServices);
            createSubscriptionPackage("Gold", new BigDecimal("349.99"), 6, 3, 180, 21, goldBenefits, goldServices);
            createSubscriptionPackage("Gold", new BigDecimal("629.99"), 12, 3, 360, 42, goldBenefits, goldServices);

            List<PlanBenefit> platinumBenefits = Arrays.asList(
                    new PlanBenefit("Unlimited access to all gyms"),
                    new PlanBenefit("Dedicated account manager"),
                    new PlanBenefit("VIP lounge access"),
                    new PlanBenefit("Weekly personal trainer sessions")
            );
            List<String> platinumServices = Arrays.asList("GYM", "SAUNA", "POOL", "SPA", "VIP");

            createSubscriptionPackage("Platinum", new BigDecimal("99.99"), 1, 4,  40,  8, platinumBenefits, platinumServices);
            createSubscriptionPackage("Platinum", new BigDecimal("269.99"), 3, 4, 120, 16, platinumBenefits, platinumServices);
            createSubscriptionPackage("Platinum", new BigDecimal("499.99"), 6, 4, 240, 30, platinumBenefits, platinumServices);
            createSubscriptionPackage("Platinum", new BigDecimal("899.99"), 12, 4, 480, 60, platinumBenefits, platinumServices);
        }
    }

    private void createSubscriptionPackage(
            String name,
            BigDecimal priceStandard,
            int durationMonths,
            int sortOrder,
            int entryLimit,
            int freezeDays,
            List<PlanBenefit> benefits,
            List<String> services) {

        BigDecimal priceDiscounted = durationMonths > 1
                ? priceStandard.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP)
                : null;

        SubscriptionPackage pkg = new SubscriptionPackage();
        pkg.setName(name);
        pkg.setPrice(priceStandard);
        pkg.setCurrency("AZN");
        pkg.setBillingPeriod(BillingPeriod.MONTHLY);
        pkg.setIsActive(true);
        pkg.setSortOrder(sortOrder);

        PackageOption option = new PackageOption();
        option.setSubscriptionPackage(pkg);
        option.setDurationMonths(durationMonths);
        option.setPriceStandard(priceStandard);
        option.setPriceDiscounted(priceDiscounted);
        option.setEntryLimit(entryLimit);
        option.setFreezeDays(freezeDays);
        option.setBenefits(benefits);

        pkg.getOptions().add(option);
        pkg = packageRepository.save(pkg);

        String packageId = pkg.getId().toString();

        String suffix = " (" + durationMonths + (durationMonths == 1 ? " ay)" : " ay)");
        createTranslationIfNotFound("SubscriptionPackage", packageId, "AZ", "name", translatePackageNameToAz(name) + suffix);
        createTranslationIfNotFound("SubscriptionPackage", packageId, "RU", "name", translatePackageNameToRu(name) + suffix);

        for (int i = 0; i < benefits.size(); i++) {
            PlanBenefit benefit = benefits.get(i);
            String benefitId = packageId + "_benefit_" + i;
            createTranslationIfNotFound("PlanBenefit", benefitId, "AZ", "description", translateBenefitToAz(benefit.getDescription()));
            createTranslationIfNotFound("PlanBenefit", benefitId, "RU", "description", translateBenefitToRu(benefit.getDescription()));
        }
    }

    private void initExpiredSubscriptionForUser1() {
        Long userId = 1L;
        SubscriptionPackage pkg = packageRepository.findAll().stream().findFirst().orElse(null);
        if (pkg == null) {
            logger.warn("No subscription package found, cannot create expired subscription for user 1.");
            return;
        }
        Long packageId = pkg.getId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAt = now.minusMonths(1);
        LocalDateTime endAt = now.minusSeconds(5);
        Integer totalLimit = 10;
        Integer remainingLimit = 0;
        Integer freezeDays = 0;

        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPackageId(packageId);
        subscription.setStatus("EXPIRED");
        subscription.setStartAt(startAt);
        subscription.setEndAt(endAt);
        subscription.setTotalLimit(totalLimit);
        subscription.setRemainingLimit(remainingLimit);
        subscription.setFrozenDaysUsed(0);
        subscription.setAllowedFreezeDays(freezeDays);

        subscriptionRepository.save(subscription);
        logger.info("Assigned expired subscription to user {} (subscriptionId={})", userId, subscription.getSubscriptionId());
    }

    private String translatePackageNameToAz(String name) {
        return name.replace("Membership", "Abunəliyi");
    }

    private String translatePackageNameToRu(String name) {
        return name.replace("Membership", "Абонемент");
    }

    private String translateBenefitToAz(String desc) {
        return switch (desc) {
            case "Access to 5 gyms" -> "5 idman zalına giriş";
            case "Email support" -> "E-poçt dəstəyi";
            case "Access to 15 gyms" -> "15 idman zalına giriş";
            case "Priority email support" -> "Prioritet e-poçt dəstəyi";
            case "Sauna access" -> "Sauna girişi";
            case "Access to all gyms" -> "Bütün idman zallarına giriş";
            case "24/7 support" -> "24/7 dəstək";
            case "Pool access" -> "Hovuz girişi";
            case "1 monthly personal trainer session" -> "Ayda 1 fərdi məşqçi sessiyası";
            case "Unlimited access to all gyms" -> "Bütün idman zallarına limitsiz giriş";
            case "Dedicated account manager" -> "Özəl hesab meneceri";
            case "VIP lounge access" -> "VIP zal girişi";
            case "Weekly personal trainer sessions" -> "Həftəlik fərdi məşqçi sessiyaları";
            default -> desc;
        };
    }

    private String translateBenefitToRu(String desc) {
        return switch (desc) {
            case "Access to 5 gyms" -> "Dostup v 5 sportzalov";
            case "Email support" -> "Podderzhka po e-mail";
            case "Access to 15 gyms" -> "Dostup v 15 sportzalov";
            case "Priority email support" -> "Prioritetnaya podderzhka po e-mail";
            case "Sauna access" -> "Dostup v saunu";
            case "Access to all gyms" -> "Dostup vo vse zaly";
            case "24/7 support" -> "Kruglosutochnaya podderzhka";
            case "Pool access" -> "Dostup v basseyn";
            case "1 monthly personal trainer session" -> "1 individualnaya trenirovka v mesyac";
            case "Unlimited access to all gyms" -> "Bezlimitnyy dostup vo vse zaly";
            case "Dedicated account manager" -> "Vydelennyy menedzher";
            case "VIP lounge access" -> "Dostup v VIP-zal";
            case "Weekly personal trainer sessions" -> "Ezhenedel'nyye individual'nyye trenirovki";
            default -> desc;
        };
    }

    private void createTranslationIfNotFound(String entityType, String entityId, String languageCode, String fieldName, String fieldValue) {
        if (!translationRepository.existsByEntityTypeAndEntityIdAndLanguageCodeAndFieldName(entityType, entityId, languageCode, fieldName)) {
            Translation translation = Translation.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .languageCode(languageCode)
                    .fieldName(fieldName)
                    .fieldValue(fieldValue)
                    .build();
            translationRepository.save(translation);
        }
    }
}
