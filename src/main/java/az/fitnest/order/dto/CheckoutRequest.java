package az.fitnest.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CheckoutRequest(
    @NotNull(message = "İdman zalı ID-si mütləqdir")
    String gym_id,

    @NotNull(message = "Paket ID-si mütləqdir")
    String package_id,

    @NotNull(message = "Müddət mütləqdir")
    Integer duration_months,

    @NotNull(message = "Ödəniş üsulu ID-si mütləqdir")
    String payment_method_id
) {}
