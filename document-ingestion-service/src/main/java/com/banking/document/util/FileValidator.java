package com.banking.document.util;

import com.banking.document.exception.UnsupportedDocumentTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class FileValidator {

    private final long maxFileSizeMb;
    private final List<String> allowedMimeTypes;

    public FileValidator(
            @Value("${document.processing.max-file-size-mb:10}") long maxFileSizeMb,
            @Value("${document.processing.allowed-mime-types}") String allowedMimeTypesStr) {
        this.maxFileSizeMb = maxFileSizeMb;
        this.allowedMimeTypes = Arrays.asList(allowedMimeTypesStr.split(","));
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        long fileSizeBytes = file.getSize();
        long maxFileSizeBytes = maxFileSizeMb * 1024 * 1024;

        if (fileSizeBytes > maxFileSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("File size %d bytes exceeds maximum allowed size %d MB",
                            fileSizeBytes, maxFileSizeMb)
            );
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !allowedMimeTypes.contains(mimeType.toLowerCase())) {
            throw new UnsupportedDocumentTypeException(mimeType);
        }

        log.debug("File validation passed - filename: {}, size: {} bytes, mimeType: {}",
                file.getOriginalFilename(), fileSizeBytes, mimeType);
    }
}
