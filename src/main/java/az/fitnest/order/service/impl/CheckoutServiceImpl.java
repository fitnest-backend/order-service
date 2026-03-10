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
        String clientSecret = "pi_" + UUID.randomUUID() + "_secret_" + UUID.randomUUID();

        // Fill payment fields for gRPC call to payment-service
        Double paymentAmount = amount.doubleValue();
        String paymentCurrency = pkg.getCurrency();
        String paymentDescription = pkg.getName() + " - " + option.getDurationMonths() + " months";
        String paymentLanguage = request.language() != null ? request.language() : "az";
        Integer isInstallment = request.is_installment() != null ? request.is_installment() : 0;
        Integer refund = request.refund() != null ? request.refund() : 0;
        java.util.List<String> otherAttr = request.other_attr() != null ? request.other_attr() : java.util.Collections.emptyList();

        // TODO: Call payment-service via gRPC with these fields
        // Example:
        // paymentGrpcClient.createPayment(orderId, paymentAmount, paymentCurrency, paymentDescription, paymentLanguage, isInstallment, refund, otherAttr);

        return CheckoutResponse.builder()
                .order_id(orderId)
                .status("pending_payment")
                .amount(paymentAmount)
                .currency(paymentCurrency)
                .payment(CheckoutPaymentInfoDto.builder()
                        .provider("stripe")
                        .payment_intent_client_secret(clientSecret)
                        .build())
                .build();
    }
}
