package az.fitnest.order.repository;

import az.fitnest.order.entity.PackageServiceOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageServiceOptionRepository extends JpaRepository<PackageServiceOption, Long> {
    List<PackageServiceOption> findByPackageId(String packageId);
}
