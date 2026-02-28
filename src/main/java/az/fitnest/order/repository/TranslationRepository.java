package az.fitnest.order.repository;

import az.fitnest.order.model.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    boolean existsByEntityTypeAndEntityIdAndLanguageCodeAndFieldName(String entityType, String entityId, String languageCode, String fieldName);
}
