package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.PackageFreezeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageFreezeRuleRepository extends JpaRepository<PackageFreezeRule, Long> {
    List<PackageFreezeRule> findByPackageId(String packageId);
    Optional<PackageFreezeRule> findByPackageIdAndDurationMonths(String packageId, Integer durationMonths);
}
