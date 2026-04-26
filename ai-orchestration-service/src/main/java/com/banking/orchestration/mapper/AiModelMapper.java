package com.banking.orchestration.mapper;

import com.banking.orchestration.domain.AiModel;
import com.banking.orchestration.dto.ModelConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiModelMapper {

    ModelConfig toModelConfig(AiModel aiModel);
}
