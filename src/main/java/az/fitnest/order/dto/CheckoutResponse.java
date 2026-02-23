package az.fitnest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private String order_id;
    private String status;
    private Double amount;
    private String currency;
    private CheckoutPaymentInfoDto payment;
}
