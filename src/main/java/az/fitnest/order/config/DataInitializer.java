package az.fitnest.order.config;

import az.fitnest.order.model.entity.*;
import az.fitnest.order.model.enums.BillingPeriod;
import az.fitnest.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
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
            // Bronze Plan
            createMembershipPlan("Bronze Membership", "19.99", 1, Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/medal-bronze.png", "Access to 5 gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/customer-support.png", "Email support")
            ), Arrays.asList("GYM"));

            // Silver Plan
            createMembershipPlan("Silver Membership", "39.99", 2, Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/medal-silver.png", "Access to 15 gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/customer-support.png", "Priority email support"),
                    new PlanBenefit("https://img.icons8.com/color/96/sauna.png", "Sauna access")
            ), Arrays.asList("GYM", "SAUNA"));

            // Gold Plan
            createMembershipPlan("Gold Membership", "69.99", 3, Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/medal-gold.png", "Access to all gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/customer-support.png", "24/7 support"),
                    new PlanBenefit("https://img.icons8.com/color/96/swimming-pool.png", "Pool access"),
                    new PlanBenefit("https://img.icons8.com/color/96/personal-trainer.png", "1 monthly personal trainer session")
            ), Arrays.asList("GYM", "SAUNA", "POOL"));

            // Platinum Plan
            createMembershipPlan("Platinum Membership", "99.99", 4, Arrays.asList(
                    new PlanBenefit("https://img.icons8.com/color/96/diamond.png", "Unlimited access to all gyms"),
                    new PlanBenefit("https://img.icons8.com/color/96/conference-call.png", "Dedicated account manager"),
                    new PlanBenefit("https://img.icons8.com/color/96/vip.png", "VIP lounge access"),
                    new PlanBenefit("https://img.icons8.com/color/96/personal-trainer.png", "Weekly personal trainer sessions")
            ), Arrays.asList("GYM", "SAUNA", "POOL", "SPA", "VIP"));
        }
    }

    private void createMembershipPlan(String name, String price, int sortOrder, List<PlanBenefit> benefits, List<String> services) {
        MembershipPlan plan = new MembershipPlan();
        plan.setName(name);
        plan.setPrice(new BigDecimal(price));
        plan.setCurrency("AZN");
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setIsActive(true);
        plan.setSortOrder(sortOrder);
        plan.setBenefits(benefits);

        DurationOption option = new DurationOption();
        option.setMembershipPlan(plan);
        option.setDurationMonths(1);
        option.setPriceStandard(new BigDecimal(price));
        option.setPriceDiscounted(new BigDecimal(price).multiply(new BigDecimal("0.9")).setScale(2, BigDecimal.ROUND_HALF_UP));
        option.setEntryLimit(sortOrder * 10);
        option.setFreezeDays(sortOrder * 2);
        option.setServices(services);

        plan.getOptions().add(option);
        plan = membershipPlanRepository.save(plan);

        String planId = plan.getId().toString();
        // Translate Plan Name
        createTranslationIfNotFound("MembershipPlan", planId, "AZ", "name", translatePlanNameToAz(name));
        createTranslationIfNotFound("MembershipPlan", planId, "RU", "name", translatePlanNameToRu(name));

        // Translate Benefits
        for (int i = 0; i < benefits.size(); i++) {
            PlanBenefit benefit = benefits.get(i);
            String benefitId = planId + "_benefit_" + i; // Or use a proper unique key if Benefit has one
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
