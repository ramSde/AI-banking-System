package com.banking.fraud.service.impl;

import com.banking.fraud.domain.AlertStatus;
import com.banking.fraud.domain.FraudAlert;
import com.banking.fraud.dto.FraudAlertResponse;
import com.banking.fraud.exception.FraudException;
import com.banking.fraud.mapper.FraudMapper;
import com.banking.fraud.repository.FraudAlertRepository;
import com.banking.fraud.service.FraudAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Fraud Alert Service Implementation
 * 
 * Manages fraud alerts and investigations.
 */
@Service
@Transactional
public class FraudAlertServiceImpl implements FraudAlertService {

    private static final Logger log = LoggerFactory.getLogger(FraudAlertServiceImpl.class);

    private final FraudAlertRepository fraudAlertRepository;
    private final FraudMapper fraudMapper;

    public FraudAlertServiceImpl(
            FraudAlertRepository fraudAlertRepository,
            FraudMapper fraudMapper
    ) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.fraudMapper = fraudMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudAlertResponse getAlertById(UUID alertId) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new FraudException("Fraud alert not found: " + alertId));

        return fraudMapper.toResponse(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getAllAlerts(Pageable pageable) {
        return fraudAlertRepository.findAllNotDeleted(pageable)
                .map(fraudMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getAlertsByStatus(AlertStatus status, Pageable pageable) {
        return fraudAlertRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status, pageable)
                .map(fraudMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getAlertsByUser(UUID userId, Pageable pageable) {
        return fraudAlertRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                .map(fraudMapper::toResponse);
    }

    @Override
    public FraudAlertResponse assignAlert(UUID alertId, UUID assignedTo) {
        log.info("Assigning fraud alert {} to user {}", alertId, assignedTo);

        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new FraudException("Fraud alert not found: " + alertId));

        if (alert.isResolved()) {
            throw new FraudException("Cannot assign resolved alert");
        }

        alert.setAssignedTo(assignedTo);
        if (alert.getStatus() == AlertStatus.OPEN) {
            alert.setStatus(AlertStatus.INVESTIGATING);
        }

        FraudAlert updatedAlert = fraudAlertRepository.save(alert);
        log.info("Assigned fraud alert {} to user {}", alertId, assignedTo);

        return fraudMapper.toResponse(updatedAlert);
    }

    @Override
    public FraudAlertResponse updateAlertStatus(UUID alertId, AlertStatus status, String resolutionNotes) {
        log.info("Updating fraud alert {} status to {}", alertId, status);

        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new FraudException("Fraud alert not found: " + alertId));

        alert.setStatus(status);

        if (status == AlertStatus.RESOLVED || status == AlertStatus.FALSE_POSITIVE) {
            alert.setResolvedAt(Instant.now());
            if (resolutionNotes != null) {
                alert.setResolutionNotes(resolutionNotes);
            }
        }

        FraudAlert updatedAlert = fraudAlertRepository.save(alert);
        log.info("Updated fraud alert {} status to {}", alertId, status);

        return fraudMapper.toResponse(updatedAlert);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getOpenAlerts(Pageable pageable) {
        return fraudAlertRepository.findOpenAlerts(pageable)
                .map(fraudMapper::toResponse);
    }
}
