package az.fitnest.order.repository;

import az.fitnest.order.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, String status);

    Optional<Subscription> findByUserIdAndGymIdAndStatus(Long userId, Long gymId, String status);
}
