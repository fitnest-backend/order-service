package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDto {
    private String status;

    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    @JsonProperty("attempted_amount")
    private BigDecimal attemptedAmount;

    private String currency;

    @JsonProperty("provider_reference")
    private String providerReference;

    @JsonProperty("failure_reason")
    private String failureReason;
}
