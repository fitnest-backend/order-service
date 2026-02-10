package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.PackageVisitLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageVisitLimitRepository extends JpaRepository<PackageVisitLimit, Long> {
    List<PackageVisitLimit> findByPackageId(String packageId);
    Optional<PackageVisitLimit> findByPackageIdAndDurationMonths(String packageId, Integer durationMonths);
}
