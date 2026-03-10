package az.fitnest.order.service.impl;

import az.fitnest.order.service.CheckoutService;
import az.fitnest.order.dto.CheckoutRequest;
import az.fitnest.order.dto.CheckoutResponse;
import az.fitnest.order.dto.CheckoutPaymentInfoDto;
import az.fitnest.order.model.entity.SubscriptionPackage;
import az.fitnest.order.model.entity.PackageOption;
import az.fitnest.order.repository.SubscriptionPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final SubscriptionPackageRepository packageRepository;
    private final az.fitnest.order.client.PaymentClient paymentClient;

    @Transactional
    @Override
    public CheckoutResponse processCheckout(Long userId, CheckoutRequest request) {
        Long packageId = Long.parseLong(request.package_id());

        SubscriptionPackage pkg = packageRepository.findById(packageId)
                .filter(SubscriptionPackage::getIsActive)
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.plan_not_found"));

        PackageOption option = pkg.getOptions().stream()
                .filter(o -> o.getId().equals(request.option_id()))
                .findFirst()
                .orElseThrow(() -> new az.fitnest.order.exception.ResourceNotFoundException("error.duration_config_not_found"));

        BigDecimal amount = option.getPriceDiscounted() != null ? option.getPriceDiscounted() : option.getPriceStandard();

        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);

        az.fitnest.order.dto.epoint.EpointPaymentRequest paymentRequest = az.fitnest.order.dto.epoint.EpointPaymentRequest.builder()
                .order_id(orderId)
                .amount(amount.doubleValue())
                .currency(pkg.getCurrency())
                .description(pkg.getName() + " - " + option.getDurationMonths() + " months")
                .language(request.language() != null ? request.language() : "az")
                .is_installment(request.is_installment() != null ? request.is_installment() : 0)
                .refund(request.refund() != null ? request.refund() : 0)
                .other_attr(request.other_attr() != null ? (java.util.List) request.other_attr() : java.util.Collections.emptyList())
                .build();

        org.springframework.http.ResponseEntity<az.fitnest.order.dto.epoint.EpointResponse> paymentResponse = paymentClient.initiatePayment(paymentRequest);
        az.fitnest.order.dto.epoint.EpointResponse epointResponse = paymentResponse.getBody();

        if (epointResponse == null || !"success".equalsIgnoreCase(epointResponse.status())) {
            throw new RuntimeException("Payment initiation failed: " + (epointResponse != null ? epointResponse.message() : "Unknown error"));
        }

        return CheckoutResponse.builder()
                .order_id(orderId)
                .status("pending_payment")
                .amount(amount.doubleValue())
                .currency(pkg.getCurrency())
                .payment(CheckoutPaymentInfoDto.builder()
                        .provider("epoint")
                        .payment_url(epointResponse.redirect_url())
                        .status(epointResponse.status())
                        .build())
                .build();
    }
}
