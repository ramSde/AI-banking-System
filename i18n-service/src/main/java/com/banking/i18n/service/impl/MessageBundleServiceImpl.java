package com.banking.i18n.service.impl;

import com.banking.i18n.domain.Translation;
import com.banking.i18n.dto.MessageBundleResponse;
import com.banking.i18n.exception.UnsupportedLocaleException;
import com.banking.i18n.repository.SupportedLocaleRepository;
import com.banking.i18n.repository.TranslationRepository;
import com.banking.i18n.service.MessageBundleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MessageBundleServiceImpl implements MessageBundleService {

    private static final Logger logger = LoggerFactory.getLogger(MessageBundleServiceImpl.class);

    private final TranslationRepository translationRepository;
    private final SupportedLocaleRepository localeRepository;

    public MessageBundleServiceImpl(TranslationRepository translationRepository,
                                    SupportedLocaleRepository localeRepository) {
        this.translationRepository = translationRepository;
        this.localeRepository = localeRepository;
    }

    @Override
    @Cacheable(value = "messageBundle", key = "#localeCode + '_' + (#category != null ? #category : 'all')")
    public MessageBundleResponse getMessageBundle(String localeCode, String category) {
        logger.debug("Fetching message bundle for locale: {}, category: {}", localeCode, category);
        
        // Validate locale exists and is enabled
        localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .filter(locale -> locale.isEnabled())
                .orElseThrow(() -> new UnsupportedLocaleException("Locale not supported or disabled: " + localeCode));
        
        List<Translation> translations;
        
        if (category != null && !category.isBlank()) {
            translations = translationRepository.findByLocaleCodeAndCategoryAndDeletedAtIsNull(localeCode, category);
        } else {
            translations = translationRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode);
        }
        
        Map<String, String> messages = translations.stream()
                .collect(Collectors.toMap(
                        Translation::getTranslationKey,
                        Translation::getValue,
                        (existing, replacement) -> existing // Keep first value in case of duplicates
                ));
        
        logger.debug("Retrieved {} messages for locale: {}", messages.size(), localeCode);
        
        return new MessageBundleResponse(
                localeCode,
                category,
                messages,
                messages.size()
        );
    }

    @Override
    @Cacheable(value = "messageBundle", key = "#localeCode + '_all'")
    public Map<String, String> getAllMessages(String localeCode) {
        logger.debug("Fetching all messages for locale: {}", localeCode);
        
        // Validate locale exists and is enabled
        localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .filter(locale -> locale.isEnabled())
                .orElseThrow(() -> new UnsupportedLocaleException("Locale not supported or disabled: " + localeCode));
        
        List<Translation> translations = translationRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode);
        
        return translations.stream()
                .collect(Collectors.toMap(
                        Translation::getTranslationKey,
                        Translation::getValue,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    @Cacheable(value = "messageBundle", key = "#localeCode + '_' + #category")
    public Map<String, String> getMessagesByCategory(String localeCode, String category) {
        logger.debug("Fetching messages for locale: {}, category: {}", localeCode, category);
        
        // Validate locale exists and is enabled
        localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .filter(locale -> locale.isEnabled())
                .orElseThrow(() -> new UnsupportedLocaleException("Locale not supported or disabled: " + localeCode));
        
        List<Translation> translations = translationRepository
                .findByLocaleCodeAndCategoryAndDeletedAtIsNull(localeCode, category);
        
        return translations.stream()
                .collect(Collectors.toMap(
                        Translation::getTranslationKey,
                        Translation::getValue,
                        (existing, replacement) -> existing
                ));
    }
}
