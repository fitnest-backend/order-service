package az.fitnest.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "package_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PackagePricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private String packageId;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "discount_price")
    private BigDecimal discountPrice;

    @Column(name = "currency", nullable = false)
    private String currency;
}
