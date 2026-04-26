package com.banking.orchestration.service;

import com.banking.orchestration.domain.AiModel;

import java.util.List;
import java.util.UUID;

public interface ModelSelectionService {

    AiModel selectModel(String feature, String modelPreference, UUID userId);

    List<AiModel> getFallbackChain(AiModel primaryModel);

    List<AiModel> getAllEnabledModels();
}
