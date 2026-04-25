package com.banking.fraud.mapper;

import com.banking.fraud.domain.FraudAlert;
import com.banking.fraud.domain.FraudCheck;
import com.banking.fraud.domain.FraudRule;
import com.banking.fraud.dto.FraudAlertResponse;
import com.banking.fraud.dto.FraudCheckResponse;
import com.banking.fraud.dto.FraudRuleRequest;
import com.banking.fraud.dto.FraudRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Fraud Mapper
 * 
 * Maps between domain entities and DTOs using MapStruct.
 */
@Mapper(componentModel = "spring")
public interface FraudMapper {

    /**
     * Map fraud rule to response DTO
     * 
     * @param fraudRule Fraud rule entity
     * @return Fraud rule response DTO
     */
    FraudRuleResponse toResponse(FraudRule fraudRule);

    /**
     * Map fraud rule request to entity
     * 
     * @param request Fraud rule request DTO
     * @return Fraud rule entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    FraudRule toEntity(FraudRuleRequest request);

    /**
     * Update fraud rule entity from request
     * 
     * @param request Fraud rule request DTO
     * @param fraudRule Fraud rule entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(FraudRuleRequest request, @MappingTarget FraudRule fraudRule);

    /**
     * Map fraud check to response DTO
     * 
     * @param fraudCheck Fraud check entity
     * @return Fraud check response DTO
     */
    FraudCheckResponse toResponse(FraudCheck fraudCheck);

    /**
     * Map fraud alert to response DTO
     * 
     * @param fraudAlert Fraud alert entity
     * @return Fraud alert response DTO
     */
    FraudAlertResponse toResponse(FraudAlert fraudAlert);
}
