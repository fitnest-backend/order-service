package az.fitnest.order.config;

import az.fitnest.order.model.entity.*;
import az.fitnest.order.model.enums.BillingPeriod;
import az.fitnest.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final MembershipPlanRepository membershipPlanRepository;
    private final TranslationRepository translationRepository;

    @Bean
    public CommandLineRunner initOrderData() {
        return args -> {
            initMembershipPlans();
        };
    }

    private void initMembershipPlans() {
        if (membershipPlanRepository.count() == 0) {

            // ── Bronze ────────────────────────────────────────────
            List<PlanBenefit> bronzeBenefits = Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/medal-bronze.png", "Access to 5 gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/customer-support.png", "Email support")
            );
            List<String> bronzeServices = Arrays.asList("GYM");

            createMembershipPlan("Bronze Membership", new BigDecimal("19.99"), 1, 1,  10,  2, bronzeBenefits, bronzeServices);
            createMembershipPlan("Bronze Membership", new BigDecimal("54.99"), 3, 1,  30,  4, bronzeBenefits, bronzeServices);
            createMembershipPlan("Bronze Membership", new BigDecimal("99.99"), 6, 1,  60,  7, bronzeBenefits, bronzeServices);
            createMembershipPlan("Bronze Membership", new BigDecimal("179.99"), 12, 1, 120, 14, bronzeBenefits, bronzeServices);

            // ── Silver ────────────────────────────────────────────
            List<PlanBenefit> silverBenefits = Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/medal-silver.png", "Access to 15 gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/customer-support.png", "Priority email support"),
                    new PlanBenefit("https://img.icons8.com/color/96/sauna.png", "Sauna access")
            );
            List<String> silverServices = Arrays.asList("GYM", "SAUNA");

            createMembershipPlan("Silver Membership", new BigDecimal("39.99"), 1, 2,  20,  4, silverBenefits, silverServices);
            createMembershipPlan("Silver Membership", new BigDecimal("109.99"), 3, 2,  60,  8, silverBenefits, silverServices);
            createMembershipPlan("Silver Membership", new BigDecimal("199.99"), 6, 2, 120, 14, silverBenefits, silverServices);
            createMembershipPlan("Silver Membership", new BigDecimal("359.99"), 12, 2, 240, 28, silverBenefits, silverServices);

            // ── Gold ──────────────────────────────────────────────
            List<PlanBenefit> goldBenefits = Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/medal-gold.png", "Access to all gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/customer-support.png", "24/7 support"),
                    new PlanBenefit("https://img.icons8.com/color/96/swimming-pool.png", "Pool access"),
                    new PlanBenefit("https://img.icons8.com/color/96/personal-trainer.png", "1 monthly personal trainer session")
            );
            List<String> goldServices = Arrays.asList("GYM", "SAUNA", "POOL");

            createMembershipPlan("Gold Membership", new BigDecimal("69.99"), 1, 3,  30,  6, goldBenefits, goldServices);
            createMembershipPlan("Gold Membership", new BigDecimal("189.99"), 3, 3,  90, 12, goldBenefits, goldServices);
            createMembershipPlan("Gold Membership", new BigDecimal("349.99"), 6, 3, 180, 21, goldBenefits, goldServices);
            createMembershipPlan("Gold Membership", new BigDecimal("629.99"), 12, 3, 360, 42, goldBenefits, goldServices);

            // ── Platinum ──────────────────────────────────────────
            List<PlanBenefit> platinumBenefits = Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/diamond.png", "Unlimited access to all gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/conference-call.png", "Dedicated account manager"),
                    new PlanBenefit("https://img.icons8.com/color/96/vip.png", "VIP lounge access"),
                    new PlanBenefit("https://img.icons8.com/color/96/personal-trainer.png", "Weekly personal trainer sessions")
            );
            List<String> platinumServices = Arrays.asList("GYM", "SAUNA", "POOL", "SPA", "VIP");

            createMembershipPlan("Platinum Membership", new BigDecimal("99.99"), 1, 4,  40,  8, platinumBenefits, platinumServices);
            createMembershipPlan("Platinum Membership", new BigDecimal("269.99"), 3, 4, 120, 16, platinumBenefits, platinumServices);
            createMembershipPlan("Platinum Membership", new BigDecimal("499.99"), 6, 4, 240, 30, platinumBenefits, platinumServices);
            createMembershipPlan("Platinum Membership", new BigDecimal("899.99"), 12, 4, 480, 60, platinumBenefits, platinumServices);
        }
    }

    /**
     * Creates a single MembershipPlan with exactly one DurationOption.
     * Each plan+option combo is a separate row — mobile shows them as individual cards.
     */
    private void createMembershipPlan(
            String name,
            BigDecimal priceStandard,
            int durationMonths,
            int sortOrder,
            int entryLimit,
            int freezeDays,
            List<PlanBenefit> benefits,
            List<String> services) {

        // Calculate ~10% discount for longer durations
        BigDecimal priceDiscounted = durationMonths > 1
                ? priceStandard.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP)
                : null;

        MembershipPlan plan = new MembershipPlan();
        plan.setName(name);
        plan.setPrice(priceStandard);
        plan.setCurrency("AZN");
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setIsActive(true);
        plan.setSortOrder(sortOrder);
        plan.setBenefits(benefits);

        DurationOption option = new DurationOption();
        option.setMembershipPlan(plan);
        option.setDurationMonths(durationMonths);
        option.setPriceStandard(priceStandard);
        option.setPriceDiscounted(priceDiscounted);
        option.setEntryLimit(entryLimit);
        option.setFreezeDays(freezeDays);

        if (services != null) {
            java.util.List<PlanService> mappedServices = new java.util.ArrayList<>();
            for (String s : services) {
                PlanService ps = new PlanService();
                ps.setName(s);
                ps.setDurationOption(option);
                mappedServices.add(ps);
            }
            option.setServices(mappedServices);
        }

        plan.getOptions().add(option);
        plan = membershipPlanRepository.save(plan);

        String planId = plan.getId().toString();

        // Translate Plan Name
        String suffix = " (" + durationMonths + (durationMonths == 1 ? " ay)" : " ay)");
        createTranslationIfNotFound("MembershipPlan", planId, "AZ", "name", translatePlanNameToAz(name) + suffix);
        createTranslationIfNotFound("MembershipPlan", planId, "RU", "name", translatePlanNameToRu(name) + suffix);

        // Translate Benefits
        for (int i = 0; i < benefits.size(); i++) {
            PlanBenefit benefit = benefits.get(i);
            String benefitId = planId + "_benefit_" + i;
            createTranslationIfNotFound("PlanBenefit", benefitId, "AZ", "description", translateBenefitToAz(benefit.getDescription()));
            createTranslationIfNotFound("PlanBenefit", benefitId, "RU", "description", translateBenefitToRu(benefit.getDescription()));
        }
    }

    private String translatePlanNameToAz(String name) {
        return name.replace("Membership", "Abunəliyi");
    }

    private String translatePlanNameToRu(String name) {
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
