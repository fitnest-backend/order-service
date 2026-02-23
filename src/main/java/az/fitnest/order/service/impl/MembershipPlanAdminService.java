package az.fitnest.order.service.impl;

import az.fitnest.order.repository.MembershipPlanRepository;
import az.fitnest.order.dto.DurationOptionEntityDto;
import az.fitnest.order.dto.MembershipPlanWithOptionsRequest;
import az.fitnest.order.enums.BillingPeriod;
import az.fitnest.order.entity.DurationOption;
import az.fitnest.order.entity.MembershipPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MembershipPlanAdminService {

    private final MembershipPlanRepository membershipPlanRepository;

    @Transactional
    public void addPlanWithOptions(Long gymId, MembershipPlanWithOptionsRequest request) {
        MembershipPlan plan = new MembershipPlan();
        plan.setGymId(gymId);
        plan.setName(request.getName());
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : "AZN");
        if (request.getBillingPeriod() != null) {
            plan.setBillingPeriod(request.getBillingPeriod());
        } else {
            plan.setBillingPeriod(BillingPeriod.MONTHLY);
        }
        plan.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        plan.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        if (request.getOptions() != null) {
            for (DurationOptionEntityDto dto : request.getOptions()) {
                DurationOption opt = new DurationOption();
                opt.setMembershipPlan(plan);
                opt.setDurationMonths(dto.getDurationMonths());
                opt.setPriceStandard(dto.getPriceStandard());
                opt.setPriceDiscounted(dto.getPriceDiscounted());
                opt.setEntryLimit(dto.getEntryLimit());
                opt.setFreezeDays(dto.getFreezeDays());
                if (dto.getServices() != null) opt.setServices(new ArrayList<>(dto.getServices()));
                plan.getOptions().add(opt);
            }
        }

        // Set a default price for compatibility
        if (!plan.getOptions().isEmpty()) {
            DurationOption first = plan.getOptions().get(0);
            if (first.getDurationMonths() != null && first.getDurationMonths() > 0 && first.getPriceStandard() != null) {
                BigDecimal monthly = first.getPriceStandard().divide(new BigDecimal(first.getDurationMonths()), 4, RoundingMode.HALF_UP);
                plan.setPrice(monthly);
            } else {
                plan.setPrice(BigDecimal.ZERO);
            }
        } else {
            plan.setPrice(BigDecimal.ZERO);
        }

        membershipPlanRepository.save(plan);
    }

    @Transactional
    public void updatePlanWithOptions(Long gymId, Long planId, MembershipPlanWithOptionsRequest request) {
        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .filter(p -> p.getGymId().equals(gymId))
                .orElseThrow(() -> new RuntimeException("Plan not found for the specified gym"));

        plan.setName(request.getName());
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : plan.getCurrency());
        if (request.getBillingPeriod() != null) {
            plan.setBillingPeriod(request.getBillingPeriod());
        }
        plan.setIsActive(request.getIsActive() != null ? request.getIsActive() : plan.getIsActive());
        plan.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : plan.getSortOrder());

        plan.getOptions().clear();
        if (request.getOptions() != null) {
            for (DurationOptionEntityDto dto : request.getOptions()) {
                DurationOption opt = new DurationOption();
                opt.setMembershipPlan(plan);
                opt.setDurationMonths(dto.getDurationMonths());
                opt.setPriceStandard(dto.getPriceStandard());
                opt.setPriceDiscounted(dto.getPriceDiscounted());
                opt.setEntryLimit(dto.getEntryLimit());
                opt.setFreezeDays(dto.getFreezeDays());
                if (dto.getServices() != null) opt.setServices(new ArrayList<>(dto.getServices()));
                plan.getOptions().add(opt);
            }
        }

        if (!plan.getOptions().isEmpty()) {
            DurationOption first = plan.getOptions().get(0);
            if (first.getDurationMonths() != null && first.getDurationMonths() > 0 && first.getPriceStandard() != null) {
                BigDecimal monthly = first.getPriceStandard().divide(new BigDecimal(first.getDurationMonths()), 4, RoundingMode.HALF_UP);
                plan.setPrice(monthly);
            }
        }

        membershipPlanRepository.save(plan);
    }
}
