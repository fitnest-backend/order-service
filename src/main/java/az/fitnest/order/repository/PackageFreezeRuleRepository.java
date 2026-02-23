package az.fitnest.order.repository;

import az.fitnest.order.entity.PackageFreezeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageFreezeRuleRepository extends JpaRepository<PackageFreezeRule, Long> {
    List<PackageFreezeRule> findByPackageId(String packageId);
    Optional<PackageFreezeRule> findByPackageIdAndDurationMonths(String packageId, Integer durationMonths);
}
