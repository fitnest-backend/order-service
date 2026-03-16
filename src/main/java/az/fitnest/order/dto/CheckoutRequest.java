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

    @Schema(description = "Ödəniş məbləği", example = "99.99")
    Double amount,

    @Schema(description = "Valyuta", example = "AZN")
    String currency,

    @Schema(hidden = true)
    String description,

    @Schema(description = "Dil", example = "az")
    String language,

    @Schema(description = "Taksit", example = "0")
    Integer is_installment,

    @Schema(description = "Refund", example = "0")
    Integer refund,

    @Schema(description = "Əlavə atributlar")
    java.util.List<String> other_attr
) {}
