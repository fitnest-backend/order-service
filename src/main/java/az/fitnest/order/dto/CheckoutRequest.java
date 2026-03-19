package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CheckoutRequest(
    @NotNull(message = "Paket ID-si mütləqdir")
    @JsonProperty("package_id")
    String package_id,

    @NotNull(message = "Option ID-si mütləqdir")
    @JsonProperty("option_id")
    Long option_id,

    /**
     * Taksit: 0 (no installment), 1 (installment). Boolean as integer.
     */
    @Schema(description = "Taksit (0: yox, 1: bəli)", example = "0")
    Integer is_installment
) {}
