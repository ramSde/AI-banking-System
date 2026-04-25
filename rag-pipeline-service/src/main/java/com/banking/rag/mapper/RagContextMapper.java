package com.banking.rag.mapper;

import com.banking.rag.domain.RagContext;
import com.banking.rag.dto.ContextAssemblyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RagContextMapper {

    @Mapping(target = "contextId", source = "id")
    ContextAssemblyResponse toContextAssemblyResponse(RagContext ragContext);
}
