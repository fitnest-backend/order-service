package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, String> {
    List<SubscriptionPackage> findByIsActiveTrue();
}
