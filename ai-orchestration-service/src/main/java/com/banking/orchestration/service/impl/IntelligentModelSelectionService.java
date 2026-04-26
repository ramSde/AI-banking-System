package com.banking.orchestration.service.impl;

import com.banking.orchestration.domain.AiModel;
import com.banking.orchestration.repository.AiModelRepository;
import com.banking.orchestration.service.ModelSelectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IntelligentModelSelectionService implements ModelSelectionService {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentModelSelectionService.class);
    private final AiModelRepository aiModelRepository;

    public IntelligentModelSelectionService(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    @Override
    public AiModel selectModel(String feature, String modelPreference, UUID userId) {
        logger.debug("Selecting model for feature: {}, preference: {}, userId: {}", feature, modelPreference, userId);

        if (modelPreference != null && !modelPreference.isBlank()) {
            return aiModelRepository.findByNameAndNotDeleted(modelPreference)
                    .filter(AiModel::getEnabled)
                    .orElseGet(() -> selectDefaultModel(feature));
        }

        return selectDefaultModel(feature);
    }

    @Override
    public List<AiModel> getFallbackChain(AiModel primaryModel) {
        List<AiModel> allModels = aiModelRepository.findAllEnabledOrderByPriority();
        
        List<AiModel> fallbackChain = new ArrayList<>();
        fallbackChain.add(primaryModel);
        
        allModels.stream()
                .filter(model -> !model.getId().equals(primaryModel.getId()))
                .filter(model -> model.getPriority() < primaryModel.getPriority())
                .limit(2)
                .forEach(fallbackChain::add);

        logger.debug("Fallback chain: {}", fallbackChain.stream()
                .map(AiModel::getName)
                .collect(Collectors.joining(" -> ")));

        return fallbackChain;
    }

    @Override
    public List<AiModel> getAllEnabledModels() {
        return aiModelRepository.findAllEnabledOrderByPriority();
    }

    private AiModel selectDefaultModel(String feature) {
        List<AiModel> enabledModels = aiModelRepository.findAllEnabledOrderByPriority();
        
        if (enabledModels.isEmpty()) {
            throw new IllegalStateException("No enabled AI models available");
        }

        return enabledModels.get(0);
    }
}
