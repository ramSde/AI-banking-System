package com.banking.orchestration.mapper;

import com.banking.orchestration.domain.AiUsage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AiUsageMapper {

    AiUsage toEntity(com.banking.orchestration.dto.AiResponse response);
}
