package com.banking.vision.util;

import com.banking.vision.exception.OcrProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image manipulation and processing operations.
 * Provides helper methods for image conversion, resizing, and format handling.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Component
public class ImageUtil {

    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final String DEFAULT_IMAGE_FORMAT = "png";

    /**
     * Convert image bytes to BufferedImage.
     *
     * @param imageBytes Image data as byte array
     * @return BufferedImage object
     * @throws OcrProcessingException if conversion fails
     */
    public BufferedImage bytesToImage(byte[] imageBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new OcrProcessingException("Failed to read image from bytes");
            }
            return image;
        } catch (IOException e) {
            log.error("Error converting bytes to image", e);
            throw new OcrProcessingException("Failed to convert bytes to image: " + e.getMessage());
        }
    }

    /**
     * Convert BufferedImage to byte array.
     *
     * @param image  BufferedImage to convert
     * @param format Image format (png, jpg, etc.)
     * @return Image data as byte array
     * @throws OcrProcessingException if conversion fails
     */
    public byte[] imageToBytes(BufferedImage image, String format) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String imageFormat = format != null ? format : DEFAULT_IMAGE_FORMAT;
            ImageIO.write(image, imageFormat, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error converting image to bytes", e);
            throw new OcrProcessingException("Failed to convert image to bytes: " + e.getMessage());
        }
    }

    /**
     * Resize image to fit within maximum dimensions while maintaining aspect ratio.
     *
     * @param image Original image
     * @return Resized image
     */
    public BufferedImage resizeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Check if resizing is needed
        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return image;
        }

        // Calculate new dimensions maintaining aspect ratio
        double scaleFactor = Math.min(
                (double) MAX_IMAGE_DIMENSION / width,
                (double) MAX_IMAGE_DIMENSION / height
        );

        int newWidth = (int) (width * scaleFactor);
        int newHeight = (int) (height * scaleFactor);

        log.debug("Resizing image from {}x{} to {}x{}", width, height, newWidth, newHeight);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();

        // Set rendering hints for better quality
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        return resizedImage;
    }

    /**
     * Convert image to grayscale.
     *
     * @param image Original image
     * @return Grayscale image
     */
    public BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D graphics = grayscaleImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        return grayscaleImage;
    }

    /**
     * Adjust image contrast.
     *
     * @param image  Original image
     * @param factor Contrast factor (1.0 = no change, >1.0 = increase, <1.0 = decrease)
     * @return Contrast-adjusted image
     */
    public BufferedImage adjustContrast(BufferedImage image, double factor) {
        BufferedImage adjustedImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                image.getType()
        );

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Apply contrast adjustment
                r = clamp((int) (factor * (r - 128) + 128));
                g = clamp((int) (factor * (g - 128) + 128));
                b = clamp((int) (factor * (b - 128) + 128));

                int newRgb = (r << 16) | (g << 8) | b;
                adjustedImage.setRGB(x, y, newRgb);
            }
        }

        return adjustedImage;
    }

    /**
     * Apply binary threshold to image (convert to black and white).
     *
     * @param image     Original image
     * @param threshold Threshold value (0-255)
     * @return Binary image
     */
    public BufferedImage applyThreshold(BufferedImage image, int threshold) {
        BufferedImage binaryImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY
        );

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Calculate grayscale value
                int gray = (r + g + b) / 3;

                // Apply threshold
                int newValue = gray >= threshold ? 255 : 0;
                int newRgb = (newValue << 16) | (newValue << 8) | newValue;
                binaryImage.setRGB(x, y, newRgb);
            }
        }

        return binaryImage;
    }

    /**
     * Rotate image by specified degrees.
     *
     * @param image   Original image
     * @param degrees Rotation angle in degrees
     * @return Rotated image
     */
    public BufferedImage rotateImage(BufferedImage image, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int width = image.getWidth();
        int height = image.getHeight();

        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D graphics = rotatedImage.createGraphics();

        graphics.translate((newWidth - width) / 2, (newHeight - height) / 2);
        graphics.rotate(radians, width / 2.0, height / 2.0);
        graphics.drawRenderedImage(image, null);
        graphics.dispose();

        return rotatedImage;
    }

    /**
     * Crop image to specified bounds.
     *
     * @param image  Original image
     * @param x      X coordinate of top-left corner
     * @param y      Y coordinate of top-left corner
     * @param width  Width of crop area
     * @param height Height of crop area
     * @return Cropped image
     */
    public BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height) {
        // Validate bounds
        x = Math.max(0, Math.min(x, image.getWidth() - 1));
        y = Math.max(0, Math.min(y, image.getHeight() - 1));
        width = Math.min(width, image.getWidth() - x);
        height = Math.min(height, image.getHeight() - y);

        return image.getSubimage(x, y, width, height);
    }

    /**
     * Get image format from content type.
     *
     * @param contentType MIME content type
     * @return Image format (png, jpg, etc.)
     */
    public String getImageFormat(String contentType) {
        if (contentType == null) {
            return DEFAULT_IMAGE_FORMAT;
        }

        return switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/tiff", "image/tif" -> "tiff";
            case "image/bmp" -> "bmp";
            case "image/gif" -> "gif";
            default -> DEFAULT_IMAGE_FORMAT;
        };
    }

    /**
     * Check if content type is a supported image format.
     *
     * @param contentType MIME content type
     * @return true if supported, false otherwise
     */
    public boolean isSupportedImageFormat(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/") &&
                (contentType.contains("jpeg") ||
                        contentType.contains("jpg") ||
                        contentType.contains("png") ||
                        contentType.contains("tiff") ||
                        contentType.contains("tif") ||
                        contentType.contains("bmp"));
    }

    /**
     * Clamp value to valid RGB range (0-255).
     *
     * @param value Value to clamp
     * @return Clamped value
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Calculate optimal threshold using Otsu's method.
     *
     * @param image Grayscale image
     * @return Optimal threshold value
     */
    public int calculateOtsuThreshold(BufferedImage image) {
        int[] histogram = new int[256];
        int totalPixels = image.getWidth() * image.getHeight();

        // Build histogram
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                histogram[gray]++;
            }
        }

        // Calculate optimal threshold
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        int wF;
        double maxVariance = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;

            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;

            double variance = wB * wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }

        return threshold;
    }
}
