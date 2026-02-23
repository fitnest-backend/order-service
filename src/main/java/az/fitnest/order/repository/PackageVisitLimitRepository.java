package az.fitnest.order.repository;

import az.fitnest.order.entity.PackageVisitLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageVisitLimitRepository extends JpaRepository<PackageVisitLimit, Long> {
    List<PackageVisitLimit> findByPackageId(String packageId);
    Optional<PackageVisitLimit> findByPackageIdAndDurationMonths(String packageId, Integer durationMonths);
}
