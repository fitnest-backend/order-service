package az.fitnest.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;
}
