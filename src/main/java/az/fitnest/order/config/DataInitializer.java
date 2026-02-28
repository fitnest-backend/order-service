package az.fitnest.order.config;

import az.fitnest.order.entity.DurationOption;
import az.fitnest.order.entity.MembershipPlan;
import az.fitnest.order.entity.PlanBenefit;
import az.fitnest.order.enums.BillingPeriod;
import az.fitnest.order.repository.MembershipPlanRepository;
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
                new PlanBenefit("bronze_icon", "Access to 5 gyms"),
                new PlanBenefit("support_icon", "Email support")
            ), Arrays.asList("GYM"));

            // Silver Plan
            createMembershipPlan("Silver Membership", "39.99", 2, Arrays.asList(
                new PlanBenefit("silver_icon", "Access to 15 gyms"),
                new PlanBenefit("support_icon", "Priority email support"),
                new PlanBenefit("sauna_icon", "Sauna access")
            ), Arrays.asList("GYM", "SAUNA"));

            // Gold Plan
            createMembershipPlan("Gold Membership", "69.99", 3, Arrays.asList(
                new PlanBenefit("gold_icon", "Access to all gyms"),
                new PlanBenefit("support_icon", "24/7 support"),
                new PlanBenefit("pool_icon", "Pool access"),
                new PlanBenefit("trainer_icon", "1 monthly personal trainer session")
            ), Arrays.asList("GYM", "SAUNA", "POOL"));

            // Platinum Plan
            createMembershipPlan("Platinum Membership", "99.99", 4, Arrays.asList(
                new PlanBenefit("platinum_icon", "Unlimited access to all gyms"),
                new PlanBenefit("support_icon", "Dedicated account manager"),
                new PlanBenefit("vip_icon", "VIP lounge access"),
                new PlanBenefit("trainer_icon", "Weekly personal trainer sessions")
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
        membershipPlanRepository.save(plan);
    }
}
