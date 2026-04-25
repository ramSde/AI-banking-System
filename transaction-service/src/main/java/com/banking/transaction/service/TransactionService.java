package com.banking.transaction.service;

import com.banking.transaction.dto.TransactionCreateRequest;
import com.banking.transaction.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transaction Service Interface
 * 
 * Core service for managing financial transactions with double-entry ledger.
 */
public interface TransactionService {

    /**
     * Create a new transaction with idempotency support
     */
    TransactionResponse createTransaction(TransactionCreateRequest request, String idempotencyKey, UUID userId, String jwtToken);

    /**
     * Get transaction by ID
     */
    TransactionResponse getTransactionById(UUID transactionId, UUID userId);

    /**
     * Get transaction by reference number
     */
    TransactionResponse getTransactionByReference(String referenceNumber, UUID userId);

    /**
     * Get user's transactions
     */
    Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable);

    /**
     * Get transactions for an account
     */
    Page<TransactionResponse> getAccountTransactions(UUID accountId, UUID userId, Pageable pageable);

    /**
     * Get transactions within date range
     */
    List<TransactionResponse> getTransactionsByDateRange(UUID userId, Instant startDate, Instant endDate);

    /**
     * Reverse a completed transaction (admin only)
     */
    TransactionResponse reverseTransaction(UUID transactionId, String reason, UUID adminId, String jwtToken);

    /**
     * Get all transactions (admin only)
     */
    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}
