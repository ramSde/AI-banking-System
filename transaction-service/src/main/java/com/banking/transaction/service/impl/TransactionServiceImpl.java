package com.banking.transaction.service.impl;

import com.banking.transaction.config.TransactionProperties;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionStatus;
import com.banking.transaction.domain.TransactionType;
import com.banking.transaction.dto.TransactionCreateRequest;
import com.banking.transaction.dto.TransactionResponse;
import com.banking.transaction.event.*;
import com.banking.transaction.exception.*;
import com.banking.transaction.mapper.TransactionMapper;
import com.banking.transaction.repository.TransactionRepository;
import com.banking.transaction.service.IdempotencyService;
import com.banking.transaction.service.LedgerService;
import com.banking.transaction.service.TransactionService;
import com.banking.transaction.util.IdempotencyValidator;
import com.banking.transaction.util.TransactionReferenceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transaction Service Implementation
 * 
 * Core service implementing transaction processing with:
 * - Idempotency guarantees
 * - Double-entry ledger
 * - Account Service integration
 * - Transaction limits validation
 * - Event publishing
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final IdempotencyService idempotencyService;
    private final LedgerService ledgerService;
    private final AccountServiceClient accountServiceClient;
    private final TransactionMapper transactionMapper;
    private final TransactionReferenceGenerator referenceGenerator;
    private final IdempotencyValidator idempotencyValidator;
    private final TransactionEventPublisher eventPublisher;
    private final TransactionProperties transactionProperties;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            IdempotencyService idempotencyService,
            LedgerService ledgerService,
            AccountServiceClient accountServiceClient,
            TransactionMapper transactionMapper,
            TransactionReferenceGenerator referenceGenerator,
            IdempotencyValidator idempotencyValidator,
            TransactionEventPublisher eventPublisher,
            TransactionProperties transactionProperties) {
        this.transactionRepository = transactionRepository;
        this.idempotencyService = idempotencyService;
        this.ledgerService = ledgerService;
        this.accountServiceClient = accountServiceClient;
        this.transactionMapper = transactionMapper;
        this.referenceGenerator = referenceGenerator;
        this.idempotencyValidator = idempotencyValidator;
        this.eventPublisher = eventPublisher;
        this.transactionProperties = transactionProperties;
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request, String idempotencyKey,
                                                 UUID userId, String jwtToken) {
        log.info("Creating transaction: type={}, amount={}, user={}", request.transactionType(), request.amount(), userId);

        validateIdempotencyKey(idempotencyKey);
        validateTransactionRequest(request);
        validateTransactionLimits(request.amount(), userId);

        Transaction transaction = buildTransaction(request, userId);
        Transaction savedTransaction = transactionRepository.save(transaction);

        try {
            processTransaction(savedTransaction, jwtToken);
            return transactionMapper.toTransactionResponse(savedTransaction);
        } catch (Exception e) {
            handleTransactionFailure(savedTransaction, e);
            throw e;
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (!idempotencyValidator.isValidKey(idempotencyKey)) {
            throw new InvalidTransactionException("Invalid idempotency key");
        }
    }

    private void validateTransactionRequest(TransactionCreateRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }

        if (request.amount().compareTo(transactionProperties.getLimits().getMaxAmount()) > 0) {
            throw TransactionLimitExceededException.maxAmount(
                    transactionProperties.getLimits().getMaxAmount(),
                    request.amount()
            );
        }

        switch (request.transactionType()) {
            case WITHDRAWAL, TRANSFER, PAYMENT -> {
                if (request.sourceAccountId() == null) {
                    throw InvalidTransactionException.missingSourceAccount();
                }
            }
        }

        switch (request.transactionType()) {
            case DEPOSIT, TRANSFER, REFUND -> {
                if (request.destinationAccountId() == null) {
                    throw InvalidTransactionException.missingDestinationAccount();
                }
            }
        }

        if (request.transactionType() == TransactionType.TRANSFER) {
            if (request.sourceAccountId().equals(request.destinationAccountId())) {
                throw InvalidTransactionException.sameSourceAndDestination();
            }
        }
    }

    private void validateTransactionLimits(BigDecimal amount, UUID userId) {
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        BigDecimal dailyTotal = transactionRepository.sumCompletedTransactionsByUserSince(userId, startOfDay);

        if (dailyTotal == null) {
            dailyTotal = BigDecimal.ZERO;
        }

        BigDecimal newTotal = dailyTotal.add(amount);
        BigDecimal dailyLimit = transactionProperties.getLimits().getDailyLimitUser();

        if (newTotal.compareTo(dailyLimit) > 0) {
            throw TransactionLimitExceededException.dailyLimit(dailyLimit, newTotal);
        }
    }

    private Transaction buildTransaction(TransactionCreateRequest request, UUID userId) {
        return Transaction.builder()
                .referenceNumber(referenceGenerator.generate())
                .transactionType(request.transactionType())
                .transactionStatus(TransactionStatus.PENDING)
                .sourceAccountId(request.sourceAccountId())
                .destinationAccountId(request.destinationAccountId())
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description())
                .metadata(request.metadata())
                .initiatedBy(userId)
                .build();
    }

    @Transactional
    protected void processTransaction(Transaction transaction, String jwtToken) {
        log.info("Processing transaction: {}", transaction.getId());

        Map<String, Object> sourceAccount = null;
        Map<String, Object> destAccount = null;
        BigDecimal sourceBalanceBefore = BigDecimal.ZERO;
        BigDecimal destBalanceBefore = BigDecimal.ZERO;

        if (transaction.getSourceAccountId() != null) {
            sourceAccount = accountServiceClient.getAccount(transaction.getSourceAccountId(), jwtToken);
            validateAccount(sourceAccount, transaction.getCurrency());
            sourceBalanceBefore = new BigDecimal(sourceAccount.get("balance").toString());

            if (sourceBalanceBefore.compareTo(transaction.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        transaction.getSourceAccountId(),
                        transaction.getAmount(),
                        sourceBalanceBefore
                );
            }
        }

        if (transaction.getDestinationAccountId() != null) {
            destAccount = accountServiceClient.getAccount(transaction.getDestinationAccountId(), jwtToken);
            validateAccount(destAccount, transaction.getCurrency());
            destBalanceBefore = new BigDecimal(destAccount.get("balance").toString());
        }

        transaction.setTransactionStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        ledgerService.createLedgerEntries(
                transaction.getId(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                sourceBalanceBefore,
                destBalanceBefore
        );

        if (transaction.getSourceAccountId() != null) {
            accountServiceClient.updateBalance(
                    transaction.getSourceAccountId(),
                    transaction.getAmount(),
                    "DEBIT",
                    jwtToken
            );
        }

        if (transaction.getDestinationAccountId() != null) {
            accountServiceClient.updateBalance(
                    transaction.getDestinationAccountId(),
                    transaction.getAmount(),
                    "CREDIT",
                    jwtToken
            );
        }

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(Instant.now());
        transactionRepository.save(transaction);

        eventPublisher.publishTransactionCreated(new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getInitiatedBy(),
                transaction.getMetadata()
        ));

        eventPublisher.publishTransactionCompleted(new TransactionCompletedEvent(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getCompletedAt()
        ));

        log.info("Transaction processed successfully: {}", transaction.getId());
    }

    private void validateAccount(Map<String, Object> account, String currency) {
        String status = account.get("accountStatus").toString();
        if (!"ACTIVE".equals(status)) {
            throw new InvalidTransactionException("Account is not active: " + status);
        }

        String accountCurrency = account.get("currency").toString();
        if (!currency.equals(accountCurrency)) {
            throw InvalidTransactionException.currencyMismatch();
        }
    }

    private void handleTransactionFailure(Transaction transaction, Exception e) {
        log.error("Transaction failed: {}", transaction.getId(), e);
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transaction.setFailedAt(Instant.now());
        transaction.setFailureReason(e.getMessage());
        transactionRepository.save(transaction);

        eventPublisher.publishTransactionFailed(new TransactionFailedEvent(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                e.getMessage(),
                transaction.getFailedAt()
        ));
    }

    @Override
    public TransactionResponse getTransactionById(UUID transactionId, UUID userId) {
        log.debug("Fetching transaction: {}", transactionId);
        Transaction transaction = transactionRepository.findByIdAndNotDeleted(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (!transaction.getInitiatedBy().equals(userId)) {
            throw new InvalidTransactionException("Access denied to transaction");
        }

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse getTransactionByReference(String referenceNumber, UUID userId) {
        log.debug("Fetching transaction by reference: {}", referenceNumber);
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new TransactionNotFoundException(referenceNumber));

        if (!transaction.getInitiatedBy().equals(userId)) {
            throw new InvalidTransactionException("Access denied to transaction");
        }

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        log.debug("Fetching transactions for user: {}", userId);
        return transactionRepository.findByInitiatedBy(userId, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    @Override
    public Page<TransactionResponse> getAccountTransactions(UUID accountId, UUID userId, Pageable pageable) {
        log.debug("Fetching transactions for account: {}", accountId);
        return transactionRepository.findByAccountId(accountId, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    @Override
    public List<TransactionResponse> getTransactionsByDateRange(UUID userId, Instant startDate, Instant endDate) {
        log.debug("Fetching transactions for user: {} between {} and {}", userId, startDate, endDate);
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate)
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse reverseTransaction(UUID transactionId, String reason, UUID adminId, String jwtToken) {
        log.info("Reversing transaction: {}", transactionId);

        Transaction originalTransaction = transactionRepository.findByIdAndNotDeleted(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (!originalTransaction.canBeReversed()) {
            throw InvalidTransactionException.cannotReverse("Transaction cannot be reversed");
        }

        Transaction reversalTransaction = Transaction.builder()
                .referenceNumber(referenceGenerator.generate())
                .transactionType(TransactionType.REVERSAL)
                .transactionStatus(TransactionStatus.PENDING)
                .sourceAccountId(originalTransaction.getDestinationAccountId())
                .destinationAccountId(originalTransaction.getSourceAccountId())
                .amount(originalTransaction.getAmount())
                .currency(originalTransaction.getCurrency())
                .description("Reversal: " + reason)
                .initiatedBy(adminId)
                .parentTransactionId(originalTransaction.getId())
                .build();

        Transaction savedReversal = transactionRepository.save(reversalTransaction);

        try {
            processTransaction(savedReversal, jwtToken);

            originalTransaction.setTransactionStatus(TransactionStatus.REVERSED);
            originalTransaction.setReversedAt(Instant.now());
            originalTransaction.setReversalReference(savedReversal.getReferenceNumber());
            transactionRepository.save(originalTransaction);

            eventPublisher.publishTransactionReversed(new TransactionReversedEvent(
                    originalTransaction.getId(),
                    originalTransaction.getReferenceNumber(),
                    savedReversal.getId(),
                    savedReversal.getReferenceNumber(),
                    originalTransaction.getAmount(),
                    originalTransaction.getCurrency(),
                    reason,
                    originalTransaction.getReversedAt()
            ));

            log.info("Transaction reversed successfully: {}", transactionId);
            return transactionMapper.toTransactionResponse(savedReversal);
        } catch (Exception e) {
            handleTransactionFailure(savedReversal, e);
            throw e;
        }
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        log.debug("Fetching all transactions (admin)");
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toTransactionResponse);
    }
}
