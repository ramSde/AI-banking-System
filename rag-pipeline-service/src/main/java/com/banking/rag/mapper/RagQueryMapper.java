package com.banking.rag.mapper;

import com.banking.rag.domain.RagQuery;
import com.banking.rag.dto.RetrievalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RagQueryMapper {

    @Mapping(target = "queryId", source = "id")
    @Mapping(target = "sources", ignore = true)
    RetrievalResponse toRetrievalResponse(RagQuery ragQuery);
}
