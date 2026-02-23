package az.fitnest.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "package_visit_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageVisitLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private String packageId;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "visit_limit", nullable = false)
    private Integer visitLimit;
}
