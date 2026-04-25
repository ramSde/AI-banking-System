package com.banking.transaction.controller;

import com.banking.transaction.dto.*;
import com.banking.transaction.service.LedgerService;
import com.banking.transaction.service.TransactionHoldService;
import com.banking.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transaction Controller
 * 
 * REST API endpoints for transaction management.
 * Handles transaction creation, queries, reversals, and holds.
 */
@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final TransactionService transactionService;
    private final LedgerService ledgerService;
    private final TransactionHoldService transactionHoldService;

    public TransactionController(
            TransactionService transactionService,
            LedgerService ledgerService,
            TransactionHoldService transactionHoldService) {
        this.transactionService = transactionService;
        this.ledgerService = ledgerService;
        this.transactionHoldService = transactionHoldService;
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {
        log.info("Creating transaction: type={}", request.transactionType());

        UUID userId = UUID.fromString(authentication.getName());
        String jwtToken = authHeader.replace("Bearer ", "");

        TransactionResponse response = transactionService.createTransaction(request, idempotencyKey, userId, jwtToken);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable UUID id,
            Authentication authentication) {
        log.debug("Fetching transaction: {}", id);

        UUID userId = UUID.fromString(authentication.getName());
        TransactionResponse response = transactionService.getTransactionById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions/reference/{referenceNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @PathVariable String referenceNumber,
            Authentication authentication) {
        log.debug("Fetching transaction by reference: {}", referenceNumber);

        UUID userId = UUID.fromString(authentication.getName());
        TransactionResponse response = transactionService.getTransactionByReference(referenceNumber, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions/my-transactions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @PageableDefault(size = 20, sort = "initiatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        log.debug("Fetching user transactions");

        UUID userId = UUID.fromString(authentication.getName());
        Page<TransactionResponse> response = transactionService.getUserTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions/{id}/ledger")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> getTransactionLedger(
            @PathVariable UUID id) {
        log.debug("Fetching ledger entries for transaction: {}", id);

        List<LedgerEntryResponse> response = ledgerService.getLedgerEntriesByTransactionId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/transactions/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionReversalRequest request,
            @RequestHeader("Authorization") String authHeader,
            Authentication authentication) {
        log.info("Reversing transaction: {}", id);

        UUID adminId = UUID.fromString(authentication.getName());
        String jwtToken = authHeader.replace("Bearer ", "");

        TransactionResponse response = transactionService.reverseTransaction(id, request.reason(), adminId, jwtToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            @PageableDefault(size = 20, sort = "initiatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Fetching all transactions (admin)");

        Page<TransactionResponse> response = transactionService.getAllTransactions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/holds")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionHoldResponse>> createHold(
            @Valid @RequestBody TransactionHoldRequest request,
            Authentication authentication) {
        log.info("Creating hold for account: {}", request.accountId());

        UUID userId = UUID.fromString(authentication.getName());
        TransactionHoldResponse response = transactionHoldService.createHold(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/holds/{id}/capture")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UUID>> captureHold(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Capturing hold: {}", id);

        UUID userId = UUID.fromString(authentication.getName());
        UUID transactionId = transactionHoldService.captureHold(id, userId);
        return ResponseEntity.ok(ApiResponse.success(transactionId));
    }

    @PostMapping("/holds/{id}/release")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> releaseHold(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Releasing hold: {}", id);

        UUID userId = UUID.fromString(authentication.getName());
        transactionHoldService.releaseHold(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/holds/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionHoldResponse>> getHold(
            @PathVariable UUID id) {
        log.debug("Fetching hold: {}", id);

        TransactionHoldResponse response = transactionHoldService.getHoldById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ledger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> queryLedger(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        log.debug("Querying ledger: accountId={}, startDate={}, endDate={}", accountId, startDate, endDate);

        List<LedgerEntryResponse> response;
        if (accountId != null && startDate != null && endDate != null) {
            response = ledgerService.getLedgerEntriesByAccountIdAndDateRange(accountId, startDate, endDate);
        } else if (accountId != null) {
            response = ledgerService.getLedgerEntriesByAccountId(accountId);
        } else {
            throw new IllegalArgumentException("Account ID is required for ledger queries");
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
