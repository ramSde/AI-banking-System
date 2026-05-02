package com.banking.i18n.service;

import com.banking.i18n.dto.MessageBundleResponse;

public interface MessageBundleService {

    MessageBundleResponse getMessageBundle(String localeCode);

    MessageBundleResponse getMessageBundleByCategory(String localeCode, String category);

    void refreshMessageBundle(String localeCode);
}
