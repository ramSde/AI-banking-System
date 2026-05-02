package com.banking.i18n.service.impl;

import com.banking.i18n.domain.SupportedLocale;
import com.banking.i18n.dto.LocaleResponse;
import com.banking.i18n.exception.UnsupportedLocaleException;
import com.banking.i18n.repository.SupportedLocaleRepository;
import com.banking.i18n.service.LocaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LocaleServiceImpl implements LocaleService {

    private static final Logger logger = LoggerFactory.getLogger(LocaleServiceImpl.class);

    private final SupportedLocaleRepository localeRepository;

    public LocaleServiceImpl(SupportedLocaleRepository localeRepository) {
        this.localeRepository = localeRepository;
    }

    @Override
    @Cacheable(value = "locales", key = "'all'")
    public List<LocaleResponse> getAllSupportedLocales() {
        logger.debug("Fetching all supported locales");
        
        List<SupportedLocale> locales = localeRepository.findAllByDeletedAtIsNull();
        
        return locales.stream()
                .map(this::toLocaleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "locales", key = "#localeCode")
    public LocaleResponse getLocaleByCode(String localeCode) {
        logger.debug("Fetching locale by code: {}", localeCode);
        
        SupportedLocale locale = localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .orElseThrow(() -> new UnsupportedLocaleException("Locale not found: " + localeCode));
        
        return toLocaleResponse(locale);
    }

    @Override
    public boolean isLocaleSupported(String localeCode) {
        logger.debug("Checking if locale is supported: {}", localeCode);
        
        return localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .map(SupportedLocale::isEnabled)
                .orElse(false);
    }

    @Override
    @Transactional
    public void enableLocale(String localeCode) {
        logger.info("Enabling locale: {}", localeCode);
        
        SupportedLocale locale = localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .orElseThrow(() -> new UnsupportedLocaleException("Locale not found: " + localeCode));
        
        locale.setEnabled(true);
        localeRepository.save(locale);
        
        logger.info("Locale enabled successfully: {}", localeCode);
    }

    @Override
    @Transactional
    public void disableLocale(String localeCode) {
        logger.info("Disabling locale: {}", localeCode);
        
        SupportedLocale locale = localeRepository.findByLocaleCodeAndDeletedAtIsNull(localeCode)
                .orElseThrow(() -> new UnsupportedLocaleException("Locale not found: " + localeCode));
        
        locale.setEnabled(false);
        localeRepository.save(locale);
        
        logger.info("Locale disabled successfully: {}", localeCode);
    }

    private LocaleResponse toLocaleResponse(SupportedLocale locale) {
        return new LocaleResponse(
                locale.getLocaleCode(),
                locale.getLanguageName(),
                locale.getNativeName(),
                locale.isRtl(),
                locale.isEnabled()
        );
    }
}
