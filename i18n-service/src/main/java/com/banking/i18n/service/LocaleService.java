package com.banking.i18n.service;

import com.banking.i18n.dto.LocaleResponse;

import java.util.List;

public interface LocaleService {

    List<LocaleResponse> getAllSupportedLocales();

    List<LocaleResponse> getAllEnabledLocales();

    LocaleResponse getLocaleByCode(String localeCode);

    LocaleResponse getDefaultLocale();

    boolean isLocaleSupported(String localeCode);

    boolean isLocaleEnabled(String localeCode);
}
