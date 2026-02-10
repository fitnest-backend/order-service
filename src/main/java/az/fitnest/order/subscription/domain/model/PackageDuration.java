package az.fitnest.order.subscription.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "package_durations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageDuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private String packageId;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;
}
