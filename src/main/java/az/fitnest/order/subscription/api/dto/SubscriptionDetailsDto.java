package az.fitnest.order.subscription.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDetailsDto {
    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("package_id")
    private String packageId;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("duration_months")
    private Integer durationMonths;

    @JsonProperty("effective_price")
    private BigDecimal effectivePrice;

    private String currency;

    @JsonProperty("total_limit")
    private Integer totalLimit;

    @JsonProperty("remaining_limit")
    private Integer remainingLimit;

    @JsonProperty("start_at")
    private LocalDateTime startAt;

    @JsonProperty("end_at")
    private LocalDateTime endAt;
}
