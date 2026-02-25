package az.fitnest.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import az.fitnest.order.enums.BillingPeriod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan extends BaseAuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    private String currency = "AZN";


    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", nullable = false)
    private BillingPeriod billingPeriod;

    @ElementCollection
    @CollectionTable(name = "membership_plan_benefits", joinColumns = @JoinColumn(name = "plan_id"))
    private List<PlanBenefit> benefits = new ArrayList<>();

    @Column(name = "service_discount_percent")
    private BigDecimal serviceDiscountPercent;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "membershipPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DurationOption> options = new ArrayList<>();
}
