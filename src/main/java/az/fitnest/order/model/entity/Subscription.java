package az.fitnest.order.model.entity;

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

    @Column(name = "gym_id")
    private Long gymId;
    
    @Column(name = "plan_id", nullable = false)
    private Long planId;
    
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
