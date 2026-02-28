package az.fitnest.order.dto;

import az.fitnest.order.model.enums.BillingPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanWithOptionsRequest {
    private String name;
    private String currency;
    private BillingPeriod billingPeriod;
    private Boolean isActive;
    private Integer sortOrder;
    private List<DurationOptionEntityDto> options;
}
