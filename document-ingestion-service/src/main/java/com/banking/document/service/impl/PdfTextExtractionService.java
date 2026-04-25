package com.banking.document.service.impl;

import com.banking.document.exception.DocumentProcessingException;
import com.banking.document.service.TextExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Slf4j
public class PdfTextExtractionService implements TextExtractionService {

    @Override
    public String extractText(InputStream inputStream, String mimeType) {
        if (!supports(mimeType)) {
            throw new DocumentProcessingException("Unsupported MIME type for PDF extraction: " + mimeType);
        }

        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            log.info("Extracted {} characters from PDF with {} pages", 
                    text.length(), document.getNumberOfPages());
            
            return text;
        } catch (Exception e) {
            log.error("Error extracting text from PDF: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to extract text from PDF", e);
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return "application/pdf".equalsIgnoreCase(mimeType);
    }
}
