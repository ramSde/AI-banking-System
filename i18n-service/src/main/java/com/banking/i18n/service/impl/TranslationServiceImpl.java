package com.banking.i18n.service.impl;

import com.banking.i18n.config.I18nProperties;
import com.banking.i18n.domain.Translation;
import com.banking.i18n.domain.TranslationKey;
import com.banking.i18n.dto.BulkTranslateRequest;
import com.banking.i18n.dto.TranslateRequest;
import com.banking.i18n.dto.TranslateResponse;
import com.banking.i18n.exception.TranslationNotFoundException;
import com.banking.i18n.exception.UnsupportedLocaleException;
import com.banking.i18n.repository.TranslationKeyRepository;
import com.banking.i18n.repository.TranslationRepository;
import com.banking.i18n.service.LocaleService;
import com.banking.i18n.service.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TranslationServiceImpl implements TranslationService {

    private final TranslationRepository translationRepository;
    private final TranslationKeyRepository translationKeyRepository;
    private final LocaleService localeService;
    private final I18nProperties i18nProperties;

    public TranslationServiceImpl(
            TranslationRepository translationRepository,
            TranslationKeyRepository translationKeyRepository,
            LocaleService localeService,
            I18nProperties i18nProperties) {
        this.translationRepository = translationRepository;
        this.translationKeyRepository = translationKeyRepository;
        this.localeService = localeService;
        this.i18nProperties = i18nProperties;
    }

    @Override
    @Cacheable(value = "translations", key = "#request.keyName + '_' + #request.localeCode")
    public TranslateResponse translate(TranslateRequest request) {
        log.debug("Translating key: {} for locale: {}", request.getKeyName(), request.getLocaleCode());

        if (!localeService.isLocaleSupported(request.getLocaleCode())) {
            throw new UnsupportedLocaleException("Locale not supported: " + request.getLocaleCode());
        }

        TranslationKey key = translationKeyRepository.findByKeyName(request.getKeyName())
                .orElseThrow(() -> new TranslationNotFoundException("Translation key not found: " + request.getKeyName()));

        Translation translation = translationRepository
                .findByTranslationKeyAndLocaleCode(key, request.getLocaleCode())
                .orElseGet(() -> handleMissingTranslation(key, request.getLocaleCode(), request.getFallbackToDefault()));

        String translatedText = translation.getTranslatedText();
        if (request.getPlaceholders() != null && !request.getPlaceholders().isEmpty()) {
            translatedText = replacePlaceholders(translatedText, request.getPlaceholders());
        }

        return TranslateResponse.builder()
                .keyName(request.getKeyName())
                .localeCode(translation.getLocaleCode())
                .translatedText(translatedText)
                .isAutoTranslated(translation.getIsAutoTranslated())
                .isFallback(!translation.getLocaleCode().equals(request.getLocaleCode()))
                .fallbackLocale(translation.getLocaleCode().equals(request.getLocaleCode()) ? null : translation.getLocaleCode())
                .build();
    }

    @Override
    public Map<String, TranslateResponse> bulkTranslate(BulkTranslateRequest request) {
        log.debug("Bulk translating {} keys for locale: {}", request.getKeyNames().size(), request.getLocaleCode());

        Map<String, TranslateResponse> results = new HashMap<>();

        for (String keyName : request.getKeyNames()) {
            try {
                TranslateRequest translateRequest = TranslateRequest.builder()
                        .keyName(keyName)
                        .localeCode(request.getLocaleCode())
                        .placeholders(request.getPlaceholders() != null ? request.getPlaceholders().get(keyName) : null)
                        .fallbackToDefault(request.getFallbackToDefault())
                        .build();

                TranslateResponse response = translate(translateRequest);
                results.put(keyName, response);
            } catch (Exception e) {
                log.error("Error translating key: {}", keyName, e);
                results.put(keyName, TranslateResponse.builder()
                        .keyName(keyName)
                        .localeCode(request.getLocaleCode())
                        .translatedText(keyName)
                        .isAutoTranslated(false)
                        .isFallback(true)
                        .build());
            }
        }

        return results;
    }

    @Override
    @Cacheable(value = "translations", key = "#keyName + '_' + #localeCode")
    public String getTranslation(String keyName, String localeCode) {
        TranslateRequest request = TranslateRequest.builder()
                .keyName(keyName)
                .localeCode(localeCode)
                .fallbackToDefault(true)
                .build();

        return translate(request).getTranslatedText();
    }

    @Override
    public String getTranslationWithPlaceholders(String keyName, String localeCode, Map<String, String> placeholders) {
        TranslateRequest request = TranslateRequest.builder()
                .keyName(keyName)
                .localeCode(localeCode)
                .placeholders(placeholders)
                .fallbackToDefault(true)
                .build();

        return translate(request).getTranslatedText();
    }

    @Override
    public List<TranslateResponse> getAllTranslationsForLocale(String localeCode) {
        log.debug("Getting all translations for locale: {}", localeCode);

        List<Translation> translations = translationRepository.findAllByLocaleCode(localeCode);

        return translations.stream()
                .map(t -> TranslateResponse.builder()
                        .keyName(t.getTranslationKey().getKeyName())
                        .localeCode(t.getLocaleCode())
                        .translatedText(t.getTranslatedText())
                        .isAutoTranslated(t.getIsAutoTranslated())
                        .isFallback(false)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "translations", key = "#keyName + '_' + #localeCode")
    public void invalidateCache(String keyName, String localeCode) {
        log.info("Invalidating cache for key: {} and locale: {}", keyName, localeCode);
    }

    @Override
    @CacheEvict(value = "translations", allEntries = true)
    public void invalidateAllCache() {
        log.info("Invalidating all translation cache");
    }

    private Translation handleMissingTranslation(TranslationKey key, String localeCode, Boolean fallbackToDefault) {
        if (Boolean.TRUE.equals(fallbackToDefault) || Boolean.TRUE.equals(i18nProperties.getFallbackToDefault())) {
            String defaultLocale = i18nProperties.getDefaultLocale();
            log.warn("Translation not found for key: {} and locale: {}, falling back to default locale: {}",
                    key.getKeyName(), localeCode, defaultLocale);

            return translationRepository.findByTranslationKeyAndLocaleCode(key, defaultLocale)
                    .orElseThrow(() -> new TranslationNotFoundException(
                            "Translation not found for key: " + key.getKeyName() + " in default locale: " + defaultLocale));
        }

        throw new TranslationNotFoundException("Translation not found for key: " + key.getKeyName() + " and locale: " + localeCode);
    }

    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }
}
