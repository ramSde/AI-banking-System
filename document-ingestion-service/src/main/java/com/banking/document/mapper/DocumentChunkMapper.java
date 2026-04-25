package com.banking.document.mapper;

import com.banking.document.domain.DocumentChunk;
import com.banking.document.dto.DocumentChunkResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentChunkMapper {

    DocumentChunkResponse toResponse(DocumentChunk documentChunk);
}
