package com.banking.document.service.impl;

import com.banking.document.exception.DocumentProcessingException;
import com.banking.document.service.TextExtractionService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Set;

@Service
@Slf4j
public class ImageTextExtractionService implements TextExtractionService {

    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    private final Tesseract tesseract;

    public ImageTextExtractionService(
            @Value("${document.processing.tesseract-data-path}") String tesseractDataPath,
            @Value("${document.processing.tesseract-language:eng}") String tesseractLanguage) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tesseractDataPath);
        this.tesseract.setLanguage(tesseractLanguage);
    }

    @Override
    public String extractText(InputStream inputStream, String mimeType) {
        if (!supports(mimeType)) {
            throw new DocumentProcessingException("Unsupported MIME type for image extraction: " + mimeType);
        }

        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new DocumentProcessingException("Failed to read image from input stream");
            }

            String text = tesseract.doOCR(image);
            
            log.info("Extracted {} characters from image using OCR", text.length());
            
            return text;
        } catch (TesseractException e) {
            log.error("Tesseract OCR error: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to extract text from image using OCR", e);
        } catch (Exception e) {
            log.error("Error extracting text from image: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to extract text from image", e);
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
    }
}
