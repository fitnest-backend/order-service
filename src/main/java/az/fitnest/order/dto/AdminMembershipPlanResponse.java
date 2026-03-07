package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminMembershipPlanResponse(
    @JsonProperty("plan_id")
    Long planId,

    String name,

    String currency,

    @JsonProperty("billing_period")
    String billingPeriod,

    @JsonProperty("is_active")
    Boolean isActive,

    @JsonProperty("sort_order")
    Integer sortOrder,

    @JsonProperty("duration_options")
    List<AdminDurationOptionResponse> durationOptions
) {

    @Builder
    public record AdminDurationOptionResponse(
        @JsonProperty("option_id")
        Long optionId,

        @JsonProperty("duration_months")
        Integer durationMonths,

        @JsonProperty("price_standard")
        BigDecimal priceStandard,

        @JsonProperty("price_discounted")
        BigDecimal priceDiscounted,

        @JsonProperty("entry_limit")
        Integer entryLimit,

        @JsonProperty("freeze_days")
        Integer freezeDays
    ) {}
}
