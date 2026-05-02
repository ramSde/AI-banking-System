package com.banking.vision.service;

import com.banking.vision.domain.OcrResult;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for OCR operations.
 * 
 * Handles text extraction from images and PDFs using Tesseract OCR.
 */
public interface OcrService {

    /**
     * Perform OCR on an image file.
     * 
     * @param imageFile Image file
     * @param documentId Document ID
     * @param pageNumber Page number
     * @return OCR result
     */
    OcrResult performOcr(File imageFile, UUID documentId, int pageNumber);

    /**
     * Perform OCR on a BufferedImage.
     * 
     * @param image BufferedImage
     * @param documentId Document ID
     * @param pageNumber Page number
     * @return OCR result
     */
    OcrResult performOcr(BufferedImage image, UUID documentId, int pageNumber);

    /**
     * Perform OCR on a PDF file (all pages).
     * 
     * @param pdfFile PDF file
     * @param documentId Document ID
     * @return List of OCR results (one per page)
     */
    List<OcrResult> performOcrOnPdf(File pdfFile, UUID documentId);

    /**
     * Get OCR results for a document.
     * 
     * @param documentId Document ID
     * @return List of OCR results
     */
    List<OcrResult> getOcrResults(UUID documentId);

    /**
     * Get OCR result for specific page.
     * 
     * @param documentId Document ID
     * @param pageNumber Page number
     * @return OCR result
     */
    OcrResult getOcrResultForPage(UUID documentId, int pageNumber);

    /**
     * Calculate average confidence score for a document.
     * 
     * @param documentId Document ID
     * @return Average confidence score
     */
    Double calculateAverageConfidence(UUID documentId);

    /**
     * Get combined text from all pages.
     * 
     * @param documentId Document ID
     * @return Combined text
     */
    String getCombinedText(UUID documentId);
}
