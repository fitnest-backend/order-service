package az.fitnest.order.subscription.adapter.persistence;

import az.fitnest.order.subscription.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
