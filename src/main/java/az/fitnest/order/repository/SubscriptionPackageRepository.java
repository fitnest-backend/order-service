package az.fitnest.order.repository;

import az.fitnest.order.model.entity.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long> {
    @Query("""
    SELECT p FROM SubscriptionPackage p
    LEFT JOIN FETCH p.options o
    LEFT JOIN FETCH o.benefits
    WHERE p.id = :id
    """)
    java.util.Optional<SubscriptionPackage> findFullById(@Param("id") Long id);

    List<SubscriptionPackage> findByIsActiveTrue();

    @Query("SELECT p FROM SubscriptionPackage p LEFT JOIN FETCH p.options WHERE p.id IN :ids")
    List<SubscriptionPackage> findAllByIdWithOptions(@Param("ids") List<Long> ids);
}
