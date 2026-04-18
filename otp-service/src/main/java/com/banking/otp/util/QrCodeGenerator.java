package com.banking.otp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * QR code generator for TOTP enrollment
 */
@Component
public class QrCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeGenerator.class);
    private static final int QR_CODE_SIZE = 300;

    /**
     * Generate QR code as Base64-encoded PNG image
     */
    public String generateQrCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
