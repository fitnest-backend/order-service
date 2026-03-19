package az.fitnest.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.fitnest.order.model.enums.BillingPeriod;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "subscription_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackage extends BaseAuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", nullable = false)
    private BillingPeriod billingPeriod;

    @Column(name = "service_discount_percent")
    private BigDecimal serviceDiscountPercent;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @OneToMany(mappedBy = "subscriptionPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PackageOption> options = new HashSet<>();
}
