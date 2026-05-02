package com.banking.i18n.service;

import com.banking.i18n.dto.TranslateRequest;
import com.banking.i18n.dto.TranslateResponse;
import com.banking.i18n.dto.BulkTranslateRequest;

import java.util.List;
import java.util.Map;

public interface TranslationService {

    TranslateResponse translate(TranslateRequest request);

    Map<String, TranslateResponse> bulkTranslate(BulkTranslateRequest request);

    String getTranslation(String keyName, String localeCode);

    String getTranslationWithPlaceholders(String keyName, String localeCode, Map<String, String> placeholders);

    List<TranslateResponse> getAllTranslationsForLocale(String localeCode);

    void invalidateCache(String keyName, String localeCode);

    void invalidateAllCache();
}
