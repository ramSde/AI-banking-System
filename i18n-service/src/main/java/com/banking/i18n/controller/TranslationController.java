package com.banking.i18n.controller;

import com.banking.i18n.dto.*;
import com.banking.i18n.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/translations")
@Tag(name = "Translation", description = "Translation management APIs")
@SecurityRequirement(name = "bearer-jwt")
public class TranslationController {

    private static final Logger logger = LoggerFactory.getLogger(TranslationController.class);

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping("/translate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Translate a single key", description = "Translates a translation key to the specified locale")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation successful",
                    content = @Content(schema = @Schema(implementation = TranslateResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Translation key not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<com.banking.i18n.dto.ApiResponse<TranslateResponse>> translate(
            @Valid @RequestBody TranslateRequest request) {
        
        logger.info("Translation request received for key: {}, locale: {}", request.key(), request.locale());
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            TranslateResponse response = translationService.translate(
                    request.key(),
                    request.locale(),
                    request.parameters()
            );
            
            logger.info("Translation successful for key: {}, locale: {}", request.key(), request.locale());
            
            return ResponseEntity.ok(new com.banking.i18n.dto.ApiResponse<>(
                    true,
                    response,
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Translation failed for key: {}, locale: {}", request.key(), request.locale(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.banking.i18n.dto.ApiResponse<>(
                            false,
                            null,
                            new com.banking.i18n.dto.ApiResponse.ErrorDetails(
                                    "TRANSLATION_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @PostMapping("/translate/bulk")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Translate multiple keys", description = "Translates multiple translation keys to the specified locale")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bulk translation successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<com.banking.i18n.dto.ApiResponse<Map<String, String>>> translateBulk(
            @Valid @RequestBody BulkTranslateRequest request) {
        
        logger.info("Bulk translation request received for {} keys, locale: {}", 
                request.keys().size(), request.locale());
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            Map<String, String> translations = translationService.translateBulk(
                    request.keys(),
                    request.locale()
            );
            
            logger.info("Bulk translation successful for {} keys", translations.size());
            
            return ResponseEntity.ok(new com.banking.i18n.dto.ApiResponse<>(
                    true,
                    translations,
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Bulk translation failed", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.banking.i18n.dto.ApiResponse<>(
                            false,
                            null,
                            new com.banking.i18n.dto.ApiResponse.ErrorDetails(
                                    "BULK_TRANSLATION_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @GetMapping("/keys")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all translation keys", description = "Retrieves all translation keys with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Keys retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<com.banking.i18n.dto.ApiResponse<Page<String>>> getAllKeys(
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.info("Fetching all translation keys, page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            Page<String> keys = translationService.getAllKeys(pageable);
            
            logger.info("Retrieved {} translation keys", keys.getTotalElements());
            
            return ResponseEntity.ok(new com.banking.i18n.dto.ApiResponse<>(
                    true,
                    keys,
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch translation keys", e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.banking.i18n.dto.ApiResponse<>(
                            false,
                            null,
                            new com.banking.i18n.dto.ApiResponse.ErrorDetails(
                                    "FETCH_KEYS_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @PostMapping("/keys")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create translation key", description = "Creates a new translation key")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Key created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Key already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<com.banking.i18n.dto.ApiResponse<String>> createKey(
            @RequestParam String key,
            @RequestParam String category,
            @RequestParam(required = false) String description) {
        
        logger.info("Creating translation key: {}, category: {}", key, category);
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            translationService.createKey(key, category, description);
            
            logger.info("Translation key created successfully: {}", key);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new com.banking.i18n.dto.ApiResponse<>(
                            true,
                            "Translation key created successfully",
                            null,
                            traceId,
                            Instant.now()
                    ));
        } catch (Exception e) {
            logger.error("Failed to create translation key: {}", key, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.banking.i18n.dto.ApiResponse<>(
                            false,
                            null,
                            new com.banking.i18n.dto.ApiResponse.ErrorDetails(
                                    "CREATE_KEY_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @PutMapping("/keys/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update translation", description = "Updates a translation for a specific key and locale")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Translation updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Key not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<com.banking.i18n.dto.ApiResponse<String>> updateTranslation(
            @PathVariable String key,
            @RequestParam String locale,
            @RequestParam String value) {
        
        logger.info("Updating translation for key: {}, locale: {}", key, locale);
        
        String traceId = UUID.randomUUID().toString();
        
        try {
            translationService.updateTranslation(key, locale, value);
            
            logger.info("Translation updated successfully for key: {}, locale: {}", key, locale);
            
            return ResponseEntity.ok(new com.banking.i18n.dto.ApiResponse<>(
                    true,
                    "Translation updated successfully",
                    null,
                    traceId,
                    Instant.now()
            ));
        } catch (Exception e) {
            logger.error("Failed to update translation for key: {}, locale: {}", key, locale, e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.banking.i18n.dto.ApiResponse<>(
                            false,
                            null,
                            new com.banking.i18n.dto.ApiResponse.ErrorDetails(
                                    "UPDATE_TRANSLATION_FAILED",
                                    e.getMessage(),
                                    null
                            ),
                            traceId,
                            Instant.now()
                    ));
        }
    }

    @DeleteMapping("/keys/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete translation key", description = "Soft deletes a translation key and all its translations")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Key deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Key not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteKey(@PathVariable String key) {
        logger.info("Deleting translation key: {}", key);
        
        try {
            translationService.deleteKey(key);
            logger.info("Translation key deleted successfully: {}", key);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete translation key: {}", key, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
