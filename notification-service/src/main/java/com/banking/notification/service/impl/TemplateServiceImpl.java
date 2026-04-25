package com.banking.notification.service.impl;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.domain.NotificationTemplate;
import com.banking.notification.dto.TemplateCreateRequest;
import com.banking.notification.dto.TemplateResponse;
import com.banking.notification.dto.TemplateUpdateRequest;
import com.banking.notification.exception.TemplateNotFoundException;
import com.banking.notification.mapper.TemplateMapper;
import com.banking.notification.repository.NotificationTemplateRepository;
import com.banking.notification.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    public TemplateServiceImpl(NotificationTemplateRepository templateRepository, TemplateMapper templateMapper) {
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
    }

    @Override
    @CacheEvict(value = "templates", allEntries = true)
    public TemplateResponse createTemplate(TemplateCreateRequest request) {
        log.info("Creating notification template with code: {}", request.templateCode());

        NotificationTemplate template = templateMapper.toEntity(request);
        template.setActive(request.active() != null ? request.active() : true);

        NotificationTemplate saved = templateRepository.save(template);
        log.info("Template created successfully with ID: {}", saved.getId());

        return templateMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = "templates", allEntries = true)
    public TemplateResponse updateTemplate(UUID id, TemplateUpdateRequest request) {
        log.info("Updating template with ID: {}", id);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + id));

        if (request.name() != null) {
            template.setName(request.name());
        }
        if (request.description() != null) {
            template.setDescription(request.description());
        }
        if (request.subject() != null) {
            template.setSubject(request.subject());
        }
        if (request.bodyTemplate() != null) {
            template.setBodyTemplate(request.bodyTemplate());
        }
        if (request.active() != null) {
            template.setActive(request.active());
        }

        NotificationTemplate updated = templateRepository.save(template);
        log.info("Template updated successfully with ID: {}", updated.getId());

        return templateMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "templates", key = "#id")
    public TemplateResponse getTemplateById(UUID id) {
        log.debug("Fetching template by ID: {}", id);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + id));

        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "templates", key = "#templateCode")
    public TemplateResponse getTemplateByCode(String templateCode) {
        log.debug("Fetching template by code: {}", templateCode);

        NotificationTemplate template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with code: " + templateCode));

        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TemplateResponse> getAllTemplates(Pageable pageable) {
        log.debug("Fetching all templates with pagination: {}", pageable);

        return templateRepository.findAllActive(pageable)
                .map(templateMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplatesByChannel(NotificationChannel channel, Pageable pageable) {
        log.debug("Fetching templates by channel: {} with pagination: {}", channel, pageable);

        return templateRepository.findByChannel(channel, pageable)
                .map(templateMapper::toResponse);
    }

    @Override
    @CacheEvict(value = "templates", allEntries = true)
    public void deleteTemplate(UUID id) {
        log.info("Soft deleting template with ID: {}", id);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + id));

        template.setDeletedAt(Instant.now());
        templateRepository.save(template);

        log.info("Template soft deleted successfully with ID: {}", id);
    }
}
