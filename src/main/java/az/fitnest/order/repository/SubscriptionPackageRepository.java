package az.fitnest.order.repository;

import az.fitnest.order.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, String> {
    List<SubscriptionPackage> findByIsActiveTrue();
}
