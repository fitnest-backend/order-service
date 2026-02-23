package az.fitnest.order.repository;

import az.fitnest.order.entity.PackagePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackagePricingRepository extends JpaRepository<PackagePricing, Long> {
    List<PackagePricing> findByPackageId(String packageId);
    Optional<PackagePricing> findByPackageIdAndDurationMonths(String packageId, Integer durationMonths);
}
