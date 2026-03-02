package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentResultDto(
    String status,
    @JsonProperty("paid_amount")
    BigDecimal paidAmount,
    @JsonProperty("attempted_amount")
    BigDecimal attemptedAmount,
    String currency,
    @JsonProperty("provider_reference")
    String providerReference,
    @JsonProperty("failure_reason")
    String failureReason
) {}
