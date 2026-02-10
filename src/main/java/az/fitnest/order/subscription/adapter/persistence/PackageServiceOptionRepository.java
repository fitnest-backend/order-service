package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.PackageServiceOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageServiceOptionRepository extends JpaRepository<PackageServiceOption, Long> {
    List<PackageServiceOption> findByPackageId(String packageId);
}
