package com.banking.transaction.service.impl;

import com.banking.transaction.domain.TransactionHold;
import com.banking.transaction.dto.TransactionHoldRequest;
import com.banking.transaction.dto.TransactionHoldResponse;
import com.banking.transaction.event.HoldCreatedEvent;
import com.banking.transaction.event.HoldReleasedEvent;
import com.banking.transaction.event.TransactionEventPublisher;
import com.banking.transaction.exception.InvalidTransactionException;
import com.banking.transaction.exception.TransactionNotFoundException;
import com.banking.transaction.mapper.TransactionMapper;
import com.banking.transaction.repository.TransactionHoldRepository;
import com.banking.transaction.service.TransactionHoldService;
import com.banking.transaction.util.TransactionReferenceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Transaction Hold Service Implementation
 * 
 * Manages authorization holds for pre-authorization flows.
 */
@Service
public class TransactionHoldServiceImpl implements TransactionHoldService {

    private static final Logger log = LoggerFactory.getLogger(TransactionHoldServiceImpl.class);

    private final TransactionHoldRepository transactionHoldRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionReferenceGenerator referenceGenerator;
    private final TransactionEventPublisher eventPublisher;

    public TransactionHoldServiceImpl(
            TransactionHoldRepository transactionHoldRepository,
            TransactionMapper transactionMapper,
            TransactionReferenceGenerator referenceGenerator,
            TransactionEventPublisher eventPublisher) {
        this.transactionHoldRepository = transactionHoldRepository;
        this.transactionMapper = transactionMapper;
        this.referenceGenerator = referenceGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransactionHoldResponse createHold(TransactionHoldRequest request, UUID userId) {
        log.info("Creating hold for account: {}", request.accountId());

        Instant expiresAt = Instant.now().plusSeconds(request.expiryHours() * 3600L);

        TransactionHold hold = TransactionHold.builder()
                .accountId(request.accountId())
                .holdReference(referenceGenerator.generate())
                .amount(request.amount())
                .currency(request.currency())
                .holdType(request.holdType())
                .description(request.description())
                .initiatedBy(userId)
                .expiresAt(expiresAt)
                .build();

        TransactionHold savedHold = transactionHoldRepository.save(hold);

        eventPublisher.publishHoldCreated(new HoldCreatedEvent(
                savedHold.getId(),
                savedHold.getAccountId(),
                savedHold.getHoldType(),
                savedHold.getAmount(),
                savedHold.getCurrency(),
                savedHold.getExpiresAt(),
                savedHold.getInitiatedBy()
        ));

        log.info("Hold created successfully: {}", savedHold.getId());
        return transactionMapper.toTransactionHoldResponse(savedHold);
    }

    @Override
    @Transactional
    public UUID captureHold(UUID holdId, UUID userId) {
        log.info("Capturing hold: {}", holdId);

        TransactionHold hold = transactionHoldRepository.findById(holdId)
                .orElseThrow(() -> new TransactionNotFoundException(holdId));

        if (hold.isCaptured()) {
            throw InvalidTransactionException.cannotReverse("Hold already captured");
        }

        if (hold.isReleased()) {
            throw InvalidTransactionException.cannotReverse("Hold already released");
        }

        if (hold.isExpired()) {
            throw InvalidTransactionException.cannotReverse("Hold expired");
        }

        UUID transactionId = UUID.randomUUID();
        hold.setCapturedTransactionId(transactionId);
        transactionHoldRepository.save(hold);

        log.info("Hold captured successfully: {}", holdId);
        return transactionId;
    }

    @Override
    @Transactional
    public void releaseHold(UUID holdId, UUID userId) {
        log.info("Releasing hold: {}", holdId);

        TransactionHold hold = transactionHoldRepository.findById(holdId)
                .orElseThrow(() -> new TransactionNotFoundException(holdId));

        if (hold.isCaptured()) {
            throw InvalidTransactionException.cannotReverse("Hold already captured");
        }

        if (hold.isReleased()) {
            throw InvalidTransactionException.cannotReverse("Hold already released");
        }

        hold.setReleasedAt(Instant.now());
        transactionHoldRepository.save(hold);

        eventPublisher.publishHoldReleased(new HoldReleasedEvent(
                hold.getId(),
                hold.getAccountId(),
                hold.getAmount(),
                hold.getCurrency(),
                false,
                null,
                hold.getReleasedAt()
        ));

        log.info("Hold released successfully: {}", holdId);
    }

    @Override
    public TransactionHoldResponse getHoldById(UUID holdId) {
        log.debug("Fetching hold: {}", holdId);
        TransactionHold hold = transactionHoldRepository.findById(holdId)
                .orElseThrow(() -> new TransactionNotFoundException(holdId));
        return transactionMapper.toTransactionHoldResponse(hold);
    }
}
