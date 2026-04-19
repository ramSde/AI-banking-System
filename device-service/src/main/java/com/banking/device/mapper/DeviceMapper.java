package com.banking.device.mapper;

import com.banking.device.domain.Device;
import com.banking.device.dto.DeviceResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Device entities to DTOs.
 * Handles data masking for sensitive fields.
 */
@Component
public class DeviceMapper {

    /**
     * Convert Device entity to DeviceResponse DTO
     */
    public DeviceResponse toResponse(Device device) {
        if (device == null) {
            return null;
        }

        return new DeviceResponse(
                device.getId(),
                device.getUserId(),
                maskFingerprint(device.getFingerprintHash()),
                device.getDeviceType(),
                device.getDeviceStatus(),
                device.getTrustScore(),
                device.getLastSeenAt(),
                device.getBrowser(),
                device.getBrowserVersion(),
                device.getOs(),
                device.getOsVersion(),
                device.getDeviceName(),
                maskIpAddress(device.getIpAddress()),
                device.getScreenResolution(),
                device.getTimezone(),
                device.getLanguage(),
                truncateUserAgent(device.getUserAgent()),
                device.getGeolocation(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    /**
     * Mask fingerprint hash (show first 8 and last 8 characters)
     */
    private String maskFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.length() < 16) {
            return "***";
        }
        return "sha256:" + fingerprint.substring(0, 8) + "..." + fingerprint.substring(fingerprint.length() - 8);
    }

    /**
     * Mask IP address (show first 3 octets, mask last octet)
     */
    private String maskIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return null;
        }

        if (ipAddress.contains(":")) {
            return ipAddress.substring(0, Math.min(ipAddress.length(), 20)) + "...";
        }

        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".***";
        }

        return ipAddress;
    }

    /**
     * Truncate user agent string (max 100 characters)
     */
    private String truncateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return null;
        }

        if (userAgent.length() <= 100) {
            return userAgent;
        }

        return userAgent.substring(0, 97) + "...";
    }
}