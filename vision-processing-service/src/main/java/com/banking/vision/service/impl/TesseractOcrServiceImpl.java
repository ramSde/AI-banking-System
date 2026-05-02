package com.banking.vision.service.impl;

import com.banking.vision.config.VisionProperties;
import com.banking.vision.domain.OcrResult;
import com.banking.vision.exception.OcrProcessingException;
import com.banking.vision.repository.OcrResultRepository;
import com.banking.vision.service.ImagePreprocessingService;
import com.banking.vision.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tesseract OCR service implementation.
 * 
 * Provides OCR capabilities using Tesseract 5.x engine.
 * Supports:
 * - Single image OCR
 * - Multi-page PDF OCR
 * - Confidence scoring
 * - Language detection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TesseractOcrServiceImpl implements OcrService {

    private final VisionProperties visionProperties;
    private final OcrResultRepository ocrResultRepository;
    private final ImagePreprocessingService preprocessingService;

    @Override
    @Transactional
    public OcrResult performOcr(File imageFile, UUID documentId, int pageNumber) {
        log.info("Performing OCR on image file: {} for document: {}, page: {}", 
            imageFile.getName(), documentId, pageNumber);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize Tesseract
            Tesseract tesseract = createTesseractInstance();
            
            // Preprocess image
            BufferedImage preprocessed = preprocessingService.preprocessImage(imageFile);
            
            // Perform OCR
            String text = tesseract.doOCR(preprocessed);
            
            // Calculate confidence (Tesseract doesn't provide direct confidence, estimate from quality)
            double confidence = estimateConfidence(text);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Create and save result
            OcrResult result = OcrResult.builder()
                .documentId(documentId)
                .rawText(text)
                .confidenceScore(confidence)
                .languageDetected(visionProperties.getOcr().getDefaultLanguage())
                .pageNumber(pageNumber)
                .processingTimeMs(processingTime)
                .ocrEngine("Tesseract 5.x")
                .build();
            
            result = ocrResultRepository.save(result);
            
            log.info("OCR completed for document: {}, page: {}, confidence: {}, time: {}ms",
                documentId, pageNumber, confidence, processingTime);
            
            return result;
            
        } catch (TesseractException e) {
            log.error("Tesseract OCR failed for document: {}, page: {}", documentId, pageNumber, e);
            throw new OcrProcessingException("OCR processing failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during OCR for document: {}, page: {}", documentId, pageNumber, e);
            throw new OcrProcessingException("Unexpected OCR error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OcrResult performOcr(BufferedImage image, UUID documentId, int pageNumber) {
        log.info("Performing OCR on BufferedImage for document: {}, page: {}", documentId, pageNumber);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize Tesseract
            Tesseract tesseract = createTesseractInstance();
            
            // Preprocess image
            BufferedImage preprocessed = preprocessingService.preprocessImage(image);
            
            // Perform OCR
            String text = tesseract.doOCR(preprocessed);
            
            // Calculate confidence
            double confidence = estimateConfidence(text);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Create and save result
            OcrResult result = OcrResult.builder()
                .documentId(documentId)
                .rawText(text)
                .confidenceScore(confidence)
                .languageDetected(visionProperties.getOcr().getDefaultLanguage())
                .pageNumber(pageNumber)
                .processingTimeMs(processingTime)
                .ocrEngine("Tesseract 5.x")
                .build();
            
            return ocrResultRepository.save(result);
            
        } catch (TesseractException e) {
            throw new OcrProcessingException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<OcrResult> performOcrOnPdf(File pdfFile, UUID documentId) {
        log.info("Performing OCR on PDF file: {} for document: {}", pdfFile.getName(), documentId);
        
        List<OcrResult> results = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            
            log.info("PDF has {} pages", pageCount);
            
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                int pageNumber = pageIndex + 1;
                
                log.debug("Processing PDF page {}/{}", pageNumber, pageCount);
                
                // Render PDF page to image (300 DPI for good quality)
                BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, 300);
                
                // Perform OCR on page
                OcrResult result = performOcr(pageImage, documentId, pageNumber);
                results.add(result);
            }
            
            log.info("Completed OCR on {} pages for document: {}", pageCount, documentId);
            
            return results;
            
        } catch (IOException e) {
            log.error("Failed to load PDF file: {}", pdfFile.getName(), e);
            throw new OcrProcessingException("Failed to load PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrResult> getOcrResults(UUID documentId) {
        log.debug("Fetching OCR results for document: {}", documentId);
        return ocrResultRepository.findByDocumentId(documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public OcrResult getOcrResultForPage(UUID documentId, int pageNumber) {
        log.debug("Fetching OCR result for document: {}, page: {}", documentId, pageNumber);
        return ocrResultRepository.findByDocumentIdAndPageNumber(documentId, pageNumber)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageConfidence(UUID documentId) {
        log.debug("Calculating average confidence for document: {}", documentId);
        Double avgConfidence = ocrResultRepository.calculateAverageConfidenceScore(documentId);
        return avgConfidence != null ? avgConfidence : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public String getCombinedText(UUID documentId) {
        log.debug("Getting combined text for document: {}", documentId);
        
        List<OcrResult> results = ocrResultRepository.findByDocumentId(documentId);
        
        if (results.isEmpty()) {
            return "";
        }
        
        // Combine text from all pages
        StringBuilder combined = new StringBuilder();
        for (OcrResult result : results) {
            if (combined.length() > 0) {
                combined.append("\n\n--- Page ").append(result.getPageNumber()).append(" ---\n\n");
            }
            combined.append(result.getRawText());
        }
        
        return combined.toString();
    }

    /**
     * Create configured Tesseract instance.
     */
    private Tesseract createTesseractInstance() {
        Tesseract tesseract = new Tesseract();
        
        // Set tessdata path
        tesseract.setDatapath(visionProperties.getOcr().getTessdataPath());
        
        // Set language
        tesseract.setLanguage(visionProperties.getOcr().getDefaultLanguage());
        
        // Set page segmentation mode
        tesseract.setPageSegMode(visionProperties.getOcr().getPageSegmentationMode());
        
        // Set OCR engine mode
        tesseract.setOcrEngineMode(visionProperties.getOcr().getEngineMode());
        
        log.debug("Tesseract configured: language={}, psm={}, oem={}", 
            visionProperties.getOcr().getDefaultLanguage(),
            visionProperties.getOcr().getPageSegmentationMode(),
            visionProperties.getOcr().getEngineMode());
        
        return tesseract;
    }

    /**
     * Estimate confidence score based on text quality.
     * 
     * This is a heuristic since Tesseract doesn't provide direct confidence.
     * Factors considered:
     * - Text length (longer is better)
     * - Alphanumeric ratio (higher is better)
     * - Special character ratio (lower is better)
     * - Word count (more words is better)
     */
    private double estimateConfidence(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        String trimmed = text.trim();
        int length = trimmed.length();
        
        // Count alphanumeric characters
        long alphanumericCount = trimmed.chars()
            .filter(Character::isLetterOrDigit)
            .count();
        
        // Count words
        String[] words = trimmed.split("\\s+");
        int wordCount = words.length;
        
        // Calculate ratios
        double alphanumericRatio = (double) alphanumericCount / length;
        double avgWordLength = (double) length / wordCount;
        
        // Base confidence on text quality indicators
        double confidence = 50.0; // Start at 50%
        
        // Boost for good alphanumeric ratio (0.7-0.9 is ideal)
        if (alphanumericRatio >= 0.7 && alphanumericRatio <= 0.9) {
            confidence += 20.0;
        } else if (alphanumericRatio >= 0.5) {
            confidence += 10.0;
        }
        
        // Boost for reasonable word count
        if (wordCount >= 10) {
            confidence += 15.0;
        } else if (wordCount >= 5) {
            confidence += 10.0;
        }
        
        // Boost for reasonable average word length (4-8 chars)
        if (avgWordLength >= 4 && avgWordLength <= 8) {
            confidence += 15.0;
        }
        
        // Cap at 100
        return Math.min(confidence, 100.0);
    }
}
