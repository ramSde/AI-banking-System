package com.banking.notification.mapper;

import com.banking.notification.domain.NotificationTemplate;
import com.banking.notification.dto.TemplateCreateRequest;
import com.banking.notification.dto.TemplateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TemplateMapper {
    NotificationTemplate toEntity(TemplateCreateRequest request);
    TemplateResponse toResponse(NotificationTemplate template);
}
