package az.fitnest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    @NotNull
    private String gym_id;
    @NotNull
    private String package_id;
    @NotNull
    private Integer duration_months;
    @NotNull
    private String payment_method_id;
}
