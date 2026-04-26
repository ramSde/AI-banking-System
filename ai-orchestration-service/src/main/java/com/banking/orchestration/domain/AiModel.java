package com.banking.orchestration.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ai_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "model_type", nullable = false, length = 50)
    private String modelType;

    @Column(name = "input_price_per_1k", nullable = false, precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal inputPricePer1k = BigDecimal.ZERO;

    @Column(name = "output_price_per_1k", nullable = false, precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal outputPricePer1k = BigDecimal.ZERO;

    @Column(name = "max_tokens", nullable = false)
    @Builder.Default
    private Integer maxTokens = 4096;

    @Column(name = "context_window", nullable = false)
    @Builder.Default
    private Integer contextWindow = 8192;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Type(JsonBinaryType.class)
    @Column(name = "capabilities", columnDefinition = "jsonb")
    private Map<String, Object> capabilities;

    @Type(JsonBinaryType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;
}
