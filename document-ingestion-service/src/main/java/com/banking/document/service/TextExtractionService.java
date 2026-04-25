package com.banking.document.service;

import java.io.InputStream;

public interface TextExtractionService {

    String extractText(InputStream inputStream, String mimeType);

    boolean supports(String mimeType);
}
