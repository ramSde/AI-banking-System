package com.banking.document.mapper;

import com.banking.document.domain.Document;
import com.banking.document.dto.DocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "downloadUrl", ignore = true)
    DocumentResponse toResponse(Document document);
}
