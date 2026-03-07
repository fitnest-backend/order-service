package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CheckoutRequest(
    @NotNull(message = "İdman zalı ID-si mütləqdir")
    @JsonProperty("gym_id")
    String gym_id,

    @NotNull(message = "Paket ID-si mütləqdir")
    @JsonProperty("package_id")
    String package_id,

    @NotNull(message = "Option ID-si mütləqdir")
    @JsonProperty("option_id")
    Long option_id,

    @NotNull(message = "Ödəniş üsulu ID-si mütləqdir")
    @JsonProperty("payment_method_id")
    String payment_method_id
) {}
