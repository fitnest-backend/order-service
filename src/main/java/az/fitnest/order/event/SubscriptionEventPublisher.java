package az.fitnest.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSubscriptionEvent(Long userId, String action, Long subscriptionId) {
        SubscriptionEvent event = new SubscriptionEvent(userId, action, subscriptionId);
        kafkaTemplate.send("subscription-events", String.valueOf(userId), event);
    }
}
