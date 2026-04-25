package com.banking.notification.mapper;

import com.banking.notification.domain.NotificationHistory;
import com.banking.notification.dto.NotificationHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {
    NotificationHistoryResponse toResponse(NotificationHistory history);
}
