package az.fitnest.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEvent implements Serializable {
    private Long userId;
    private String action;
    private Long subscriptionId;
}
