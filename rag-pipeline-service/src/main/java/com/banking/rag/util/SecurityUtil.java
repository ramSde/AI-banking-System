package com.banking.rag.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return UUID.fromString((String) authentication.getPrincipal());
        }
        throw new IllegalStateException("No authenticated user found");
    }

    public static String getCurrentUserIdAsString() {
        return getCurrentUserId().toString();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
