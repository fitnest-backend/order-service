package az.fitnest.order.subscription.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "package_id", nullable = false)
    private String packageId;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    
    @Column(name = "end_at")
    private LocalDateTime endAt;
    
    @Column(name = "total_limit")
    private Integer totalLimit;
    
    @Column(name = "remaining_limit")
    private Integer remainingLimit;
}
