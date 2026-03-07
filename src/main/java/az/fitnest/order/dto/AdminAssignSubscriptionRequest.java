package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AdminAssignSubscriptionRequest(
    @NotNull(message = "error.missing_field")
    @JsonProperty("user_id")
    Long userId,

    @NotNull(message = "error.missing_field")
    @JsonProperty("plan_id")
    Long planId,

    @NotNull(message = "error.missing_field")
    @Min(value = 1, message = "error.out_of_range")
    @JsonProperty("duration_months")
    Integer durationMonths,

    @JsonProperty("gym_id")
    Long gymId
) {}

