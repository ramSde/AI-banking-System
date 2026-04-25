package com.banking.notification.service;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.dto.TemplateCreateRequest;
import com.banking.notification.dto.TemplateResponse;
import com.banking.notification.dto.TemplateUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TemplateService {
    TemplateResponse createTemplate(TemplateCreateRequest request);
    TemplateResponse updateTemplate(UUID id, TemplateUpdateRequest request);
    TemplateResponse getTemplateById(UUID id);
    TemplateResponse getTemplateByCode(String templateCode);
    Page<TemplateResponse> getAllTemplates(Pageable pageable);
    Page<TemplateResponse> getTemplatesByChannel(NotificationChannel channel, Pageable pageable);
    void deleteTemplate(UUID id);
}
