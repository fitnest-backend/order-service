package az.fitnest.order.subscription.adapter.service;

import az.fitnest.order.subscription.api.dto.PaymentResultDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MockPaymentService {

    public PaymentResultDto processPayment(String paymentMethodId, BigDecimal amount, String currency) {
        // Mock logic: 
        // If paymentMethodId starts with "pm_error", fail.
        // Else success.
        
        if (paymentMethodId.startsWith("pm_fail")) {
             return PaymentResultDto.builder()
                    .status("failed")
                    .attemptedAmount(amount)
                    .currency(currency)
                    .failureReason("insufficient_funds")
                    .build();
        }

        return PaymentResultDto.builder()
                .status("success")
                .paidAmount(amount)
                .currency(currency)
                .providerReference("trx_" + UUID.randomUUID().toString())
                .build();
    }
}
