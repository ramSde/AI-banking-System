package com.banking.orchestration.service.impl;

import com.banking.orchestration.domain.AiModel;
import com.banking.orchestration.domain.AiUsage;
import com.banking.orchestration.dto.AiRequest;
import com.banking.orchestration.dto.AiResponse;
import com.banking.orchestration.event.AiRequestCompletedEvent;
import com.banking.orchestration.event.AiRequestFailedEvent;
import com.banking.orchestration.event.AiRequestStartedEvent;
import com.banking.orchestration.exception.ModelUnavailableException;
import com.banking.orchestration.service.*;
import com.banking.orchestration.util.TokenCalculator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiOrchestrationServiceImpl implements AiOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(AiOrchestrationServiceImpl.class);
    
    private final ModelSelectionService modelSelectionService;
    private final CostControlService costControlService;
    private final QuotaManagementService quotaManagementService;
    private final UsageTrackingService usageTrackingService;
    private final OpenAiChatModel openAiChatModel;
    private final WebClient anthropicWebClient;
    private final WebClient ollamaWebClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TokenCalculator tokenCalculator;

    public AiOrchestrationServiceImpl(ModelSelectionService modelSelectionService,
                                      CostControlService costControlService,
                                      QuotaManagementService quotaManagementService,
                                      UsageTrackingService usageTrackingService,
                                      OpenAiChatModel openAiChatModel,
                                      WebClient anthropicWebClient,
                                      WebClient ollamaWebClient,
                                      KafkaTemplate<String, Object> kafkaTemplate,
                                      TokenCalculator tokenCalculator) {
        this.modelSelectionService = modelSelectionService;
        this.costControlService = costControlService;
        this.quotaManagementService = quotaManagementService;
        this.usageTrackingService = usageTrackingService;
        this.openAiChatModel = openAiChatModel;
        this.anthropicWebClient = anthropicWebClient;
        this.ollamaWebClient = ollamaWebClient;
        this.kafkaTemplate = kafkaTemplate;
        this.tokenCalculator = tokenCalculator;
    }

    @Override
    @CircuitBreaker(name = "aiOrchestration", fallbackMethod = "orchestrateFallback")
    public AiResponse orchestrate(AiRequest request, UUID userId, String traceId) {
        Instant startTime = Instant.now();
        String feature = request.feature() != null ? request.feature() : "GENERAL";
        
        AiModel selectedModel = modelSelectionService.selectModel(
                feature, request.modelPreference(), userId);
        
        logger.info("Orchestrating AI request for user: {}, model: {}, feature: {}, traceId: {}",
                userId, selectedModel.getName(), feature, traceId);

        Integer estimatedTokens = tokenCalculator.estimateTokens(request.prompt());
        BigDecimal estimatedCost = costControlService.calculateCost(
                selectedModel.getName(), estimatedTokens, estimatedTokens / 2);

        quotaManagementService.checkQuota(userId, estimatedTokens);
        costControlService.checkBudget(userId, estimatedCost);

        publishRequestStartedEvent(userId, request.sessionId(), feature, 
                selectedModel.getName(), selectedModel.getProvider(), traceId);

        List<AiModel> fallbackChain = modelSelectionService.getFallbackChain(selectedModel);
        
        for (AiModel model : fallbackChain) {
            try {
                AiResponse response = executeModelRequest(model, request, userId, startTime, traceId);
                
                quotaManagementService.recordTokenUsage(userId, response.totalTokens());
                costControlService.recordCost(userId, response.costUsd());
                
                recordUsage(userId, request.sessionId(), feature, model, response, true, null, traceId);
                publishRequestCompletedEvent(userId, request.sessionId(), feature, model.getName(),
                        model.getProvider(), response.inputTokens(), response.outputTokens(),
                        response.totalTokens(), response.latencyMs(), response.costUsd(), traceId);
                
                return response;
            } catch (Exception e) {
                logger.warn("Model {} failed, trying fallback: {}", model.getName(), e.getMessage());
                
                if (model.equals(fallbackChain.get(fallbackChain.size() - 1))) {
                    long latencyMs = Duration.between(startTime, Instant.now()).toMillis();
                    recordUsage(userId, request.sessionId(), feature, model, null, false, e.getMessage(), traceId);
                    publishRequestFailedEvent(userId, request.sessionId(), feature, model.getName(),
                            model.getProvider(), "MODEL_EXECUTION_FAILED", e.getMessage(), latencyMs, traceId);
                    throw new ModelUnavailableException("All models in fallback chain failed", model.getName(), e);
                }
            }
        }

        throw new ModelUnavailableException("No models available", selectedModel.getName());
    }

    private AiResponse executeModelRequest(AiModel model, AiRequest request, UUID userId,
                                           Instant startTime, String traceId) {
        String response;
        Integer inputTokens;
        Integer outputTokens;

        if ("openai".equalsIgnoreCase(model.getProvider())) {
            response = executeOpenAiRequest(request.prompt());
            inputTokens = tokenCalculator.countTokens(request.prompt());
            outputTokens = tokenCalculator.countTokens(response);
        } else if ("anthropic".equalsIgnoreCase(model.getProvider())) {
            response = executeAnthropicRequest(request.prompt());
            inputTokens = tokenCalculator.countTokens(request.prompt());
            outputTokens = tokenCalculator.countTokens(response);
        } else if ("ollama".equalsIgnoreCase(model.getProvider())) {
            response = executeOllamaRequest(request.prompt());
            inputTokens = tokenCalculator.countTokens(request.prompt());
            outputTokens = tokenCalculator.countTokens(response);
        } else {
            throw new IllegalArgumentException("Unsupported provider: " + model.getProvider());
        }

        Integer totalTokens = inputTokens + outputTokens;
        BigDecimal cost = costControlService.calculateCost(model.getName(), inputTokens, outputTokens);
        long latencyMs = Duration.between(startTime, Instant.now()).toMillis();

        Map<String, Object> metadata = new HashMap<>();
        if (request.metadata() != null) {
            metadata.putAll(request.metadata());
        }
        metadata.put("modelId", model.getId().toString());
        metadata.put("modelType", model.getModelType());

        return new AiResponse(
                response,
                model.getName(),
                model.getProvider(),
                inputTokens,
                outputTokens,
                totalTokens,
                latencyMs,
                cost,
                request.sessionId(),
                traceId,
                Instant.now(),
                metadata
        );
    }

    private String executeOpenAiRequest(String prompt) {
        ChatResponse response = openAiChatModel.call(new Prompt(prompt));
        return response.getResult().getOutput().getContent();
    }

    private String executeAnthropicRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-sonnet-20240229");
        requestBody.put("max_tokens", 1024);
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return anthropicWebClient.post()
                .uri("/v1/messages")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .map(resp -> {
                    List<Map<String, Object>> content = (List<Map<String, Object>>) resp.get("content");
                    return (String) content.get(0).get("text");
                })
                .block();
    }

    private String executeOllamaRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama2");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        return ollamaWebClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(120))
                .map(resp -> (String) resp.get("response"))
                .block();
    }

    private void recordUsage(UUID userId, String sessionId, String feature, AiModel model,
                             AiResponse response, boolean success, String errorMessage, String traceId) {
        AiUsage.AiUsageBuilder usageBuilder = AiUsage.builder()
                .userId(userId)
                .sessionId(sessionId)
                .feature(feature)
                .modelName(model.getName())
                .provider(model.getProvider())
                .success(success)
                .traceId(traceId);

        if (response != null) {
            usageBuilder
                    .inputTokens(response.inputTokens())
                    .outputTokens(response.outputTokens())
                    .totalTokens(response.totalTokens())
                    .latencyMs(response.latencyMs())
                    .costUsd(response.costUsd());
        } else {
            usageBuilder
                    .inputTokens(0)
                    .outputTokens(0)
                    .totalTokens(0)
                    .latencyMs(0L)
                    .costUsd(BigDecimal.ZERO)
                    .errorMessage(errorMessage);
        }

        usageTrackingService.recordUsage(usageBuilder.build());
    }

    private void publishRequestStartedEvent(UUID userId, String sessionId, String feature,
                                            String modelName, String provider, String traceId) {
        AiRequestStartedEvent event = AiRequestStartedEvent.create(
                userId, sessionId, feature, modelName, provider, traceId);
        kafkaTemplate.send("banking.ai.request-started", userId.toString(), event);
    }

    private void publishRequestCompletedEvent(UUID userId, String sessionId, String feature,
                                              String modelName, String provider, Integer inputTokens,
                                              Integer outputTokens, Integer totalTokens, Long latencyMs,
                                              BigDecimal costUsd, String traceId) {
        AiRequestCompletedEvent event = AiRequestCompletedEvent.create(
                userId, sessionId, feature, modelName, provider, inputTokens, outputTokens,
                totalTokens, latencyMs, costUsd, traceId);
        kafkaTemplate.send("banking.ai.request-completed", userId.toString(), event);
    }

    private void publishRequestFailedEvent(UUID userId, String sessionId, String feature,
                                           String modelName, String provider, String errorCode,
                                           String errorMessage, Long latencyMs, String traceId) {
        AiRequestFailedEvent event = AiRequestFailedEvent.create(
                userId, sessionId, feature, modelName, provider, errorCode, errorMessage, latencyMs, traceId);
        kafkaTemplate.send("banking.ai.request-failed", userId.toString(), event);
    }

    private AiResponse orchestrateFallback(AiRequest request, UUID userId, String traceId, Throwable t) {
        logger.error("Circuit breaker fallback triggered for user: {}, traceId: {}", userId, traceId, t);
        throw new ModelUnavailableException("AI service temporarily unavailable", "ALL_MODELS", t);
    }
}
