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
    @NotNull(message = "İdman zalı ID-si mütləqdir")
    private String gym_id;
    @NotNull(message = "Paket ID-si mütləqdir")
    private String package_id;
    @NotNull(message = "Müddət mütləqdir")
    private Integer duration_months;
    @NotNull(message = "Ödəniş üsulu ID-si mütləqdir")
    private String payment_method_id;
}
