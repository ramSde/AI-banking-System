package com.banking.vision.util;

import com.banking.vision.exception.OcrProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for PDF processing operations.
 * Provides helper methods for PDF text extraction, page rendering, and metadata extraction.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Component
public class PdfUtil {

    private static final int DEFAULT_DPI = 300;
    private static final int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

    /**
     * Extract text from PDF document.
     *
     * @param pdfBytes PDF document as byte array
     * @return Extracted text
     * @throws OcrProcessingException if extraction fails
     */
    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.debug("Extracted {} characters from PDF", text.length());
            return text;
        } catch (IOException e) {
            log.error("Error extracting text from PDF", e);
            throw new OcrProcessingException("Failed to extract text from PDF: " + e.getMessage());
        }
    }

    /**
     * Extract text from specific page of PDF document.
     *
     * @param pdfBytes   PDF document as byte array
     * @param pageNumber Page number (1-based)
     * @return Extracted text from specified page
     * @throws OcrProcessingException if extraction fails
     */
    public String extractTextFromPage(byte[] pdfBytes, int pageNumber) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                throw new OcrProcessingException("Invalid page number: " + pageNumber);
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Error extracting text from PDF page {}", pageNumber, e);
            throw new OcrProcessingException("Failed to extract text from PDF page: " + e.getMessage());
        }
    }

    /**
     * Convert PDF pages to images.
     *
     * @param pdfBytes PDF document as byte array
     * @return List of BufferedImage objects, one per page
     * @throws OcrProcessingException if conversion fails
     */
    public List<BufferedImage> convertToImages(byte[] pdfBytes) {
        return convertToImages(pdfBytes, DEFAULT_DPI);
    }

    /**
     * Convert PDF pages to images with specified DPI.
     *
     * @param pdfBytes PDF document as byte array
     * @param dpi      Resolution in dots per inch
     * @return List of BufferedImage objects, one per page
     * @throws OcrProcessingException if conversion fails
     */
    public List<BufferedImage> convertToImages(byte[] pdfBytes, int dpi) {
        List<BufferedImage> images = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            log.debug("Converting {} PDF pages to images at {} DPI", pageCount, dpi);

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi, IMAGE_TYPE);
                images.add(image);
            }

            return images;
        } catch (IOException e) {
            log.error("Error converting PDF to images", e);
            throw new OcrProcessingException("Failed to convert PDF to images: " + e.getMessage());
        }
    }

    /**
     * Convert specific PDF page to image.
     *
     * @param pdfBytes   PDF document as byte array
     * @param pageNumber Page number (1-based)
     * @return BufferedImage of the specified page
     * @throws OcrProcessingException if conversion fails
     */
    public BufferedImage convertPageToImage(byte[] pdfBytes, int pageNumber) {
        return convertPageToImage(pdfBytes, pageNumber, DEFAULT_DPI);
    }

    /**
     * Convert specific PDF page to image with specified DPI.
     *
     * @param pdfBytes   PDF document as byte array
     * @param pageNumber Page number (1-based)
     * @param dpi        Resolution in dots per inch
     * @return BufferedImage of the specified page
     * @throws OcrProcessingException if conversion fails
     */
    public BufferedImage convertPageToImage(byte[] pdfBytes, int pageNumber, int dpi) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                throw new OcrProcessingException("Invalid page number: " + pageNumber);
            }

            PDFRenderer renderer = new PDFRenderer(document);
            int pageIndex = pageNumber - 1;

            log.debug("Converting PDF page {} to image at {} DPI", pageNumber, dpi);

            return renderer.renderImageWithDPI(pageIndex, dpi, IMAGE_TYPE);
        } catch (IOException e) {
            log.error("Error converting PDF page {} to image", pageNumber, e);
            throw new OcrProcessingException("Failed to convert PDF page to image: " + e.getMessage());
        }
    }

    /**
     * Get number of pages in PDF document.
     *
     * @param pdfBytes PDF document as byte array
     * @return Number of pages
     * @throws OcrProcessingException if operation fails
     */
    public int getPageCount(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            log.error("Error getting PDF page count", e);
            throw new OcrProcessingException("Failed to get PDF page count: " + e.getMessage());
        }
    }

    /**
     * Check if PDF is encrypted.
     *
     * @param pdfBytes PDF document as byte array
     * @return true if encrypted, false otherwise
     * @throws OcrProcessingException if operation fails
     */
    public boolean isEncrypted(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return document.isEncrypted();
        } catch (IOException e) {
            log.error("Error checking PDF encryption", e);
            throw new OcrProcessingException("Failed to check PDF encryption: " + e.getMessage());
        }
    }

    /**
     * Check if PDF contains text (not scanned image).
     *
     * @param pdfBytes PDF document as byte array
     * @return true if contains text, false otherwise
     * @throws OcrProcessingException if operation fails
     */
    public boolean containsText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null && !text.trim().isEmpty();
        } catch (IOException e) {
            log.error("Error checking PDF text content", e);
            throw new OcrProcessingException("Failed to check PDF text content: " + e.getMessage());
        }
    }

    /**
     * Validate PDF document.
     *
     * @param pdfBytes PDF document as byte array
     * @return true if valid, false otherwise
     */
    public boolean isValidPdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return document.getNumberOfPages() > 0;
        } catch (IOException e) {
            log.warn("Invalid PDF document", e);
            return false;
        }
    }

    /**
     * Get PDF metadata.
     *
     * @param pdfBytes PDF document as byte array
     * @return PDF metadata as string
     * @throws OcrProcessingException if operation fails
     */
    public String getMetadata(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            var info = document.getDocumentInformation();
            StringBuilder metadata = new StringBuilder();

            if (info.getTitle() != null) {
                metadata.append("Title: ").append(info.getTitle()).append("\n");
            }
            if (info.getAuthor() != null) {
                metadata.append("Author: ").append(info.getAuthor()).append("\n");
            }
            if (info.getSubject() != null) {
                metadata.append("Subject: ").append(info.getSubject()).append("\n");
            }
            if (info.getCreator() != null) {
                metadata.append("Creator: ").append(info.getCreator()).append("\n");
            }
            if (info.getProducer() != null) {
                metadata.append("Producer: ").append(info.getProducer()).append("\n");
            }
            if (info.getCreationDate() != null) {
                metadata.append("Creation Date: ").append(info.getCreationDate()).append("\n");
            }

            metadata.append("Pages: ").append(document.getNumberOfPages());

            return metadata.toString();
        } catch (IOException e) {
            log.error("Error getting PDF metadata", e);
            throw new OcrProcessingException("Failed to get PDF metadata: " + e.getMessage());
        }
    }

    /**
     * Extract first page as image (useful for thumbnails).
     *
     * @param pdfBytes PDF document as byte array
     * @return BufferedImage of first page
     * @throws OcrProcessingException if operation fails
     */
    public BufferedImage extractFirstPageAsImage(byte[] pdfBytes) {
        return convertPageToImage(pdfBytes, 1);
    }

    /**
     * Check if PDF requires OCR (is scanned document).
     *
     * @param pdfBytes PDF document as byte array
     * @return true if OCR is needed, false otherwise
     */
    public boolean requiresOcr(byte[] pdfBytes) {
        return !containsText(pdfBytes);
    }
}
