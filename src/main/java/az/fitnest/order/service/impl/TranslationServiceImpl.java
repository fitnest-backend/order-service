package az.fitnest.order.service.impl;

import az.fitnest.order.model.entity.Translation;
import az.fitnest.order.repository.TranslationRepository;
import az.fitnest.order.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;

    @Override
    public String getTranslatedValue(String entityType, String entityId, String fieldName, String languageCode) {
        if (languageCode == null || languageCode.equalsIgnoreCase("AZ")) {
            return null;
        }

        return translationRepository.findFirstByEntityTypeAndEntityIdAndLanguageCodeAndFieldName(
                        entityType.toUpperCase(),
                        entityId,
                        languageCode.toUpperCase(),
                        fieldName)
                .map(Translation::getFieldValue)
                .orElse(null);
    }
}
