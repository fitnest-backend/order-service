package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.PackageDuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageDurationRepository extends JpaRepository<PackageDuration, Long> {
    List<PackageDuration> findByPackageId(String packageId);
}
