package com.banking.audit.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpAddressExtractor {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ipAddress = request.getHeader(header);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.split(",")[0].trim();
                }
                return ipAddress;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
