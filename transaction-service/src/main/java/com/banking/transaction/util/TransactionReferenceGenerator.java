package com.banking.transaction.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Transaction Reference Generator
 * 
 * Generates unique transaction reference numbers in format:
 * TXN-YYYYMMDD-XXXXXXXXXX (e.g., TXN-20240425-A1B2C3D4E5)
 */
@Component
public class TransactionReferenceGenerator {

    private static final String PREFIX = "TXN";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    public String generate() {
        String datePart = DATE_FORMATTER.format(Instant.now());
        String randomPart = generateRandomString();
        return String.format("%s-%s-%s", PREFIX, datePart, randomPart);
    }

    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
