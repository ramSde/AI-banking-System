package com.banking.i18n.controller;

import com.banking.i18n.dto.ApiResponse;
import com.banking.i18n.dto.LocaleResponse;
import com.banking.i18n.dto.MessageBundleResponse;
import com.banking.i18n.service.LocaleService;
import com.banking.i18n.service.MessageBundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/locales")
@Tag(name = "Locale", description = "Locale management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class LocaleController {

    private static final Logger logger = LoggerFactory.getLogger(LocaleController.class);

    private final LocaleService localeService;
    private final MessageBundleService messageBundleService;

    public LocaleController(LocaleService localeService, MessageBundleService messageBundleService) {
        this.localeService = localeService;
        this.messageBundleService = messageBundleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get all supported locales", description = "Retrieves all supported locales")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Locales retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LocaleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<LocaleResponse>>> getAllLocales() {
        logger.info("Fetching all supported locales");
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            List<LocaleResponse> locales = localeService.getAllSupportedLocales();
            
            logger.info("Retrieved {} supported locales", locales.size());
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    locales,
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch supported locales", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            null,
                            new ApiResponse.ErrorDetails(
                                    "FETCH_LOCALES_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @GetMapping("/{localeCode}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get locale by code", description = "Retrieves a specific locale by its code")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Locale retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Locale not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<LocaleResponse>> getLocale(@PathVariable String localeCode) {
        logger.info("Fetching locale: {}", localeCode);
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            LocaleResponse locale = localeService.getLocaleByCode(localeCode);
            
            if (locale == null) {
                logger.warn("Locale not found: {}", localeCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(
                                false,
                                null,
                                new ApiResponse.ErrorDetails(
                                        "LOCALE_NOT_FOUND",
                                        "Locale not found: " + localeCode,
                                        null
                                ),
                                traceId,
                                Instant.now()
                        ));
            }
            
            logger.info("Locale retrieved successfully: {}", localeCode);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    locale,
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch locale: {}", localeCode, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            null,
                            new ApiResponse.ErrorDetails(
                                    "FETCH_LOCALE_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @GetMapping("/{localeCode}/messages")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get message bundle", description = "Retrieves all translations for a specific locale")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Message bundle retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Locale not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<MessageBundleResponse>> getMessageBundle(
            @PathVariable String localeCode,
            @RequestParam(required = false) String category) {
        
        logger.info("Fetching message bundle for locale: {}, category: {}", localeCode, category);
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            MessageBundleResponse bundle = messageBundleService.getMessageBundle(localeCode, category);
            
            logger.info("Message bundle retrieved successfully for locale: {}, messages count: {}", 
                    localeCode, bundle.messages().size());
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    bundle,
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch message bundle for locale: {}", localeCode, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            null,
                            new ApiResponse.ErrorDetails(
                                    "FETCH_BUNDLE_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @PostMapping("/{localeCode}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable locale", description = "Enables a specific locale")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Locale enabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Locale not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<String>> enableLocale(@PathVariable String localeCode) {
        logger.info("Enabling locale: {}", localeCode);
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            localeService.enableLocale(localeCode);
            
            logger.info("Locale enabled successfully: {}", localeCode);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Locale enabled successfully",
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to enable locale: {}", localeCode, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            null,
                            new ApiResponse.ErrorDetails(
                                    "ENABLE_LOCALE_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @PostMapping("/{localeCode}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable locale", description = "Disables a specific locale")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Locale disabled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Locale not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<String>> disableLocale(@PathVariable String localeCode) {
        logger.info("Disabling locale: {}", localeCode);
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            localeService.disableLocale(localeCode);
            
            logger.info("Locale disabled successfully: {}", localeCode);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Locale disabled successfully",
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to disable locale: {}", localeCode, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            false,
                            null,
                            new ApiResponse.ErrorDetails(
                                    "DISABLE_LOCALE_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }
}
