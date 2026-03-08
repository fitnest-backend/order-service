package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SubscriptionDetailsDto(
    @JsonProperty("subscription_id")
    Long subscriptionId,
    @JsonProperty("package_id")
    String packageId,
    @JsonProperty("package_name")
    String packageName,
    @JsonProperty("duration_months")
    Integer durationMonths,
    @JsonProperty("effective_price")
    BigDecimal effectivePrice,
    String currency,
    @JsonProperty("total_limit")
    Integer totalLimit,
    @JsonProperty("remaining_limit")
    Integer remainingLimit,
    @JsonProperty("start_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate startAt,
    @JsonProperty("end_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate endAt,
    @JsonProperty("frozen_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate frozenAt,
    @JsonProperty("unfreezes_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate unfreezesAt,
    @JsonProperty("frozen_days_used")
    Integer frozenDaysUsed,
    @JsonProperty("allowed_freeze_days")
    Integer allowedFreezeDays,
    @JsonProperty("remaining_freeze_days")
    Integer remainingFreezeDays
) {}
