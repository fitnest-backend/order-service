package az.fitnest.order.repository;

import az.fitnest.order.model.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, String status);
    List<Subscription> findByStatusInAndEndAtBefore(List<String> statuses, LocalDateTime now);
    List<Subscription> findByStatusIn(List<String> statuses);
    List<Subscription> findByStatusInAndEndAtBetween(List<String> statuses, LocalDateTime start, LocalDateTime end);
    List<Subscription> findByStatus(String status);
    List<Subscription> findByPackageId(Long packageId);
    List<Subscription> findByIsUpgraded(Boolean isUpgraded);
}
