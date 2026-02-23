package az.fitnest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeOptionsResponse {
    private SubscriptionDetailsDto current;
    private List<UpgradeOptionDto> options;
}
