package az.fitnest.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "membership_plan_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DurationOption extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private MembershipPlan membershipPlan;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "price_standard")
    private BigDecimal priceStandard;

    @Column(name = "price_discounted")
    private BigDecimal priceDiscounted;

    @Column(name = "entry_limit")
    private Integer entryLimit;

    @Column(name = "freeze_days")
    private Integer freezeDays;

    @ElementCollection
    @CollectionTable(name = "membership_plan_option_services", joinColumns = @JoinColumn(name = "option_id"))
    @Column(name = "service")
    private List<String> services = new ArrayList<>();
}
