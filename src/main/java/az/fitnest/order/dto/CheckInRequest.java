package az.fitnest.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CheckInRequest(
    @NotNull(message = "Zal ID-si mütləqdir")
    Long gymId
) {}
