package com.banking.orchestration.service;

import com.banking.orchestration.dto.AiRequest;
import com.banking.orchestration.dto.AiResponse;

import java.util.UUID;

public interface AiOrchestrationService {

    AiResponse orchestrate(AiRequest request, UUID userId, String traceId);
}
