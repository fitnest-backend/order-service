package az.fitnest.order.service.impl;

import az.fitnest.order.service.CheckoutService;
import az.fitnest.order.dto.CheckoutRequest;
import az.fitnest.order.dto.CheckoutResponse;
import az.fitnest.order.dto.CheckoutPaymentInfoDto;
import az.fitnest.order.entity.SubscriptionPackage;
import az.fitnest.order.entity.PackagePricing;
import az.fitnest.order.repository.SubscriptionPackageRepository;
import az.fitnest.order.repository.PackagePricingRepository;
import az.fitnest.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final SubscriptionPackageRepository packageRepository;
    private final PackagePricingRepository pricingRepository;

    @Transactional
    @Override
    public CheckoutResponse processCheckout(Long userId, CheckoutRequest request) {
        // Validate package exists and is active
        SubscriptionPackage pkg = packageRepository.findById(request.getPackage_id())
                .filter(SubscriptionPackage::getIsActive)
                .orElseThrow(() -> new RuntimeException("Package not found or inactive: " + request.getPackage_id()));

        // Fetch pricing for the requested duration
        PackagePricing pricing = pricingRepository.findByPackageIdAndDurationMonths(request.getPackage_id(), request.getDuration_months())
                .orElseThrow(() -> new RuntimeException("Pricing not found for package " + request.getPackage_id() + " and duration " + request.getDuration_months()));

        BigDecimal amount = pricing.getDiscountPrice() != null ? pricing.getDiscountPrice() : pricing.getBasePrice();

        // Simulate order creation and Stripe setup
        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);
        String clientSecret = "pi_" + UUID.randomUUID().toString() + "_secret_" + UUID.randomUUID().toString();

        return CheckoutResponse.builder()
                .order_id(orderId)
                .status("pending_payment")
                .amount(amount.doubleValue())
                .currency(pricing.getCurrency())
                .payment(CheckoutPaymentInfoDto.builder()
                        .provider("stripe")
                        .payment_intent_client_secret(clientSecret)
                        .build())
                .build();
    }
}
