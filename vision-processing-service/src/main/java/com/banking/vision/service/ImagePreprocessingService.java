package com.banking.vision.service;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Service interface for image preprocessing.
 * 
 * Enhances image quality before OCR to improve accuracy:
 * - Deskewing
 * - Noise reduction
 * - Contrast enhancement
 * - Binarization
 */
public interface ImagePreprocessingService {

    /**
     * Preprocess image for OCR.
     * 
     * Applies all preprocessing steps in optimal order.
     * 
     * @param image Input image
     * @return Preprocessed image
     */
    BufferedImage preprocessImage(BufferedImage image);

    /**
     * Preprocess image file.
     * 
     * @param imageFile Input image file
     * @return Preprocessed image
     */
    BufferedImage preprocessImage(File imageFile);

    /**
     * Convert image to grayscale.
     * 
     * @param image Input image
     * @return Grayscale image
     */
    BufferedImage convertToGrayscale(BufferedImage image);

    /**
     * Apply binarization (convert to black and white).
     * 
     * @param image Input image
     * @return Binarized image
     */
    BufferedImage binarize(BufferedImage image);

    /**
     * Remove noise from image.
     * 
     * @param image Input image
     * @return Denoised image
     */
    BufferedImage denoise(BufferedImage image);

    /**
     * Deskew image (correct rotation).
     * 
     * @param image Input image
     * @return Deskewed image
     */
    BufferedImage deskew(BufferedImage image);

    /**
     * Enhance contrast.
     * 
     * @param image Input image
     * @return Contrast-enhanced image
     */
    BufferedImage enhanceContrast(BufferedImage image);

    /**
     * Resize image to optimal size for OCR.
     * 
     * @param image Input image
     * @param targetWidth Target width
     * @return Resized image
     */
    BufferedImage resize(BufferedImage image, int targetWidth);

    /**
     * Save preprocessed image to file.
     * 
     * @param image Preprocessed image
     * @param outputFile Output file
     */
    void saveImage(BufferedImage image, File outputFile);
}
