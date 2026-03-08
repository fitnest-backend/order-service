package az.fitnest.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "membership_plan_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanService extends BaseAuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private PackageOption packageOption;
}
