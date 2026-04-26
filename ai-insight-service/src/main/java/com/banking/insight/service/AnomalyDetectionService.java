package com.banking.insight.service;

import com.banking.insight.domain.Anomaly;

import java.util.List;
import java.util.UUID;

public interface AnomalyDetectionService {

    List<Anomaly> detectAnomalies(UUID userId);

    List<Anomaly> getUnacknowledgedAnomalies(UUID userId);

    List<Anomaly> getCriticalAnomalies(UUID userId);

    Anomaly getAnomalyById(UUID anomalyId, UUID userId);

    Anomaly acknowledgeAnomaly(UUID anomalyId, UUID userId, String notes);

    Anomaly markAsFalsePositive(UUID anomalyId, UUID userId, String notes);
}
