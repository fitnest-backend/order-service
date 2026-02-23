package az.fitnest.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "package_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackageServiceOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private String packageId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;
}
