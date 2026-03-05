package az.fitnest.order.service.impl;

import az.fitnest.order.repository.MembershipPlanRepository;
import az.fitnest.order.dto.DurationOptionEntityDto;
import az.fitnest.order.dto.MembershipPlanWithOptionsRequest;
import az.fitnest.order.model.enums.BillingPeriod;
import az.fitnest.order.model.entity.DurationOption;
import az.fitnest.order.model.entity.MembershipPlan;
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
    public void addPlanWithOptions(MembershipPlanWithOptionsRequest request) {
        validatePlanName(request.name());
        MembershipPlan plan = new MembershipPlan();
        plan.setName(request.name());
        plan.setCurrency(request.currency() != null ? request.currency() : "AZN");
        if (request.billingPeriod() != null) {
            plan.setBillingPeriod(request.billingPeriod());
        } else {
            plan.setBillingPeriod(BillingPeriod.MONTHLY);
        }
        plan.setIsActive(request.isActive() != null ? request.isActive() : true);
        plan.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);

        if (request.options() != null) {
            for (DurationOptionEntityDto dto : request.options()) {
                DurationOption opt = new DurationOption();
                opt.setMembershipPlan(plan);
                opt.setDurationMonths(dto.durationMonths());
                opt.setPriceStandard(dto.priceStandard());
                opt.setPriceDiscounted(dto.priceDiscounted());
                opt.setEntryLimit(dto.entryLimit());
                opt.setFreezeDays(dto.freezeDays());
                if (dto.services() != null) {
                    java.util.List<az.fitnest.order.model.entity.PlanService> services = new ArrayList<>();
                    for (az.fitnest.order.dto.PlanServiceDto psd : dto.services()) {
                        az.fitnest.order.model.entity.PlanService ps = new az.fitnest.order.model.entity.PlanService();
                        ps.setName(psd.name());
                        ps.setDurationOption(opt);
                        services.add(ps);
                    }
                    opt.setServices(services);
                }
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
    public void updatePlanWithOptions(Long planId, MembershipPlanWithOptionsRequest request) {
        validatePlanName(request.name());
        MembershipPlan plan = membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("PLAN_NOT_FOUND", "error.plan_not_found"));

        plan.setName(request.name());
        plan.setCurrency(request.currency() != null ? request.currency() : plan.getCurrency());
        if (request.billingPeriod() != null) {
            plan.setBillingPeriod(request.billingPeriod());
        }
        plan.setIsActive(request.isActive() != null ? request.isActive() : plan.getIsActive());
        plan.setSortOrder(request.sortOrder() != null ? request.sortOrder() : plan.getSortOrder());

        plan.getOptions().clear();
        if (request.options() != null) {
            for (DurationOptionEntityDto dto : request.options()) {
                DurationOption opt = new DurationOption();
                opt.setMembershipPlan(plan);
                opt.setDurationMonths(dto.durationMonths());
                opt.setPriceStandard(dto.priceStandard());
                opt.setPriceDiscounted(dto.priceDiscounted());
                opt.setEntryLimit(dto.entryLimit());
                opt.setFreezeDays(dto.freezeDays());
                if (dto.services() != null) {
                    java.util.List<az.fitnest.order.model.entity.PlanService> services = new ArrayList<>();
                    for (az.fitnest.order.dto.PlanServiceDto psd : dto.services()) {
                        az.fitnest.order.model.entity.PlanService ps = new az.fitnest.order.model.entity.PlanService();
                        ps.setName(psd.name());
                        ps.setDurationOption(opt);
                        services.add(ps);
                    }
                    opt.setServices(services);
                }
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

    private void validatePlanName(String name) {
        if (name == null) {
            throw new az.fitnest.order.exception.ServiceException("error.invalid_plan_name", "INVALID_PLAN_NAME", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        String lowerName = name.trim().toLowerCase();
        boolean isValid = lowerName.equals("bronze") || lowerName.equals("silver") 
                       || lowerName.equals("gold") || lowerName.equals("platinum")
                       || lowerName.equals("bronze membership") || lowerName.equals("silver membership") 
                       || lowerName.equals("gold membership") || lowerName.equals("platinum membership");
        
        if (!isValid) {
            throw new az.fitnest.order.exception.ServiceException("error.invalid_plan_name", "INVALID_PLAN_NAME", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
}
