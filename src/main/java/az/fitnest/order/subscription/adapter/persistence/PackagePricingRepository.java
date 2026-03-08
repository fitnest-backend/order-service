package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.PackagePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackagePricingRepository extends JpaRepository<PackagePricing, Long> {
    List<PackagePricing> findByPackageId(String packageId);
    Optional<PackagePricing> findByPackageIdAndDurationMonths(String packageId, Integer durationMonths);
}
