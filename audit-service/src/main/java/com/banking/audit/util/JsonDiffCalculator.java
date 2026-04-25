package com.banking.audit.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JsonDiffCalculator {

    private static final Logger logger = LoggerFactory.getLogger(JsonDiffCalculator.class);
    private final ObjectMapper objectMapper;

    public JsonDiffCalculator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> calculateDiff(Map<String, Object> beforeState, Map<String, Object> afterState) {
        if (beforeState == null && afterState == null) {
            return new HashMap<>();
        }

        if (beforeState == null) {
            return Map.of("operation", "CREATE", "changes", afterState != null ? afterState : new HashMap<>());
        }

        if (afterState == null) {
            return Map.of("operation", "DELETE", "changes", beforeState);
        }

        try {
            JsonNode beforeNode = objectMapper.valueToTree(beforeState);
            JsonNode afterNode = objectMapper.valueToTree(afterState);

            JsonNode patchNode = JsonDiff.asJson(beforeNode, afterNode);

            @SuppressWarnings("unchecked")
            Map<String, Object> diff = objectMapper.convertValue(patchNode, Map.class);

            return Map.of(
                    "operation", "UPDATE",
                    "patch", diff
            );

        } catch (Exception e) {
            logger.error("Error calculating JSON diff: {}", e.getMessage(), e);
            return Map.of(
                    "operation", "UPDATE",
                    "error", "Failed to calculate diff: " + e.getMessage()
            );
        }
    }
}
