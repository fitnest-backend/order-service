package az.fitnest.order.service.impl;

import az.fitnest.order.service.CheckoutService;
import az.fitnest.order.dto.CheckoutRequest;
import az.fitnest.order.dto.CheckoutResponse;
import az.fitnest.order.dto.CheckoutPaymentInfoDto;
import az.fitnest.order.entity.MembershipPlan;
import az.fitnest.order.entity.DurationOption;
import az.fitnest.order.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final MembershipPlanRepository planRepository;

    @Transactional
    @Override
    public CheckoutResponse processCheckout(Long userId, CheckoutRequest request) {
        Long planId = Long.parseLong(request.getPackage_id());

        // Validate plan exists and is active
        MembershipPlan plan = planRepository.findById(planId)
                .filter(MembershipPlan::getIsActive)
                .orElseThrow(() -> new RuntimeException("Plan not found or inactive: " + planId));

        // Find the matching duration option
        DurationOption option = plan.getOptions().stream()
                .filter(o -> o.getDurationMonths().equals(request.getDuration_months()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No option for duration " + request.getDuration_months() + " on plan " + planId));

        BigDecimal amount = option.getPriceDiscounted() != null ? option.getPriceDiscounted() : option.getPriceStandard();

        // Simulate order creation and Stripe setup
        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);
        String clientSecret = "pi_" + UUID.randomUUID() + "_secret_" + UUID.randomUUID();

        return CheckoutResponse.builder()
                .order_id(orderId)
                .status("pending_payment")
                .amount(amount.doubleValue())
                .currency(plan.getCurrency())
                .payment(CheckoutPaymentInfoDto.builder()
                        .provider("stripe")
                        .payment_intent_client_secret(clientSecret)
                        .build())
                .build();
    }
}
