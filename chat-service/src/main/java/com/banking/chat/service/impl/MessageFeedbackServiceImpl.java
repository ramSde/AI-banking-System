package com.banking.chat.service.impl;

import com.banking.chat.domain.ChatMessage;
import com.banking.chat.domain.ChatSession;
import com.banking.chat.domain.MessageFeedback;
import com.banking.chat.dto.FeedbackResponse;
import com.banking.chat.dto.SubmitFeedbackRequest;
import com.banking.chat.event.MessageFeedbackSubmittedEvent;
import com.banking.chat.exception.InvalidSessionException;
import com.banking.chat.exception.MessageNotFoundException;
import com.banking.chat.exception.SessionNotFoundException;
import com.banking.chat.repository.ChatMessageRepository;
import com.banking.chat.repository.ChatSessionRepository;
import com.banking.chat.repository.MessageFeedbackRepository;
import com.banking.chat.service.KafkaProducerService;
import com.banking.chat.service.MessageFeedbackService;
import com.banking.chat.util.FeedbackMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class MessageFeedbackServiceImpl implements MessageFeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(MessageFeedbackServiceImpl.class);

    private final MessageFeedbackRepository feedbackRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final KafkaProducerService kafkaProducerService;
    private final FeedbackMapper feedbackMapper;

    public MessageFeedbackServiceImpl(
            MessageFeedbackRepository feedbackRepository,
            ChatMessageRepository messageRepository,
            ChatSessionRepository sessionRepository,
            KafkaProducerService kafkaProducerService,
            FeedbackMapper feedbackMapper) {
        this.feedbackRepository = feedbackRepository;
        this.messageRepository = messageRepository;
        this.sessionRepository = sessionRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    public FeedbackResponse submitFeedback(UUID userId, SubmitFeedbackRequest request) {
        logger.info("Submitting feedback for message: {} by user: {}", request.messageId(), userId);

        ChatMessage message = messageRepository.findByIdAndNotDeleted(request.messageId())
                .orElseThrow(() -> new MessageNotFoundException(request.messageId()));

        ChatSession session = sessionRepository.findByIdAndNotDeleted(message.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(message.getSessionId()));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Message does not belong to user");
        }

        MessageFeedback feedback = MessageFeedback.builder()
                .messageId(request.messageId())
                .userId(userId)
                .rating(request.rating())
                .comment(request.comment())
                .metadata(request.metadata())
                .build();

        feedback = feedbackRepository.save(feedback);
        logger.info("Submitted feedback: {} for message: {}", feedback.getId(), request.messageId());

        MessageFeedbackSubmittedEvent event = new MessageFeedbackSubmittedEvent(
                feedback.getId(),
                request.messageId(),
                userId,
                request.rating(),
                request.comment()
        );
        kafkaProducerService.publishMessageFeedbackSubmittedEvent(event);

        return feedbackMapper.toResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(UUID feedbackId, UUID userId) {
        logger.debug("Fetching feedback: {} for user: {}", feedbackId, userId);

        MessageFeedback feedback = feedbackRepository.findByIdAndNotDeleted(feedbackId)
                .orElseThrow(() -> new MessageNotFoundException("Feedback not found: " + feedbackId));

        if (!feedback.getUserId().equals(userId)) {
            throw new InvalidSessionException("Feedback does not belong to user");
        }

        return feedbackMapper.toResponse(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getUserFeedback(UUID userId, Pageable pageable) {
        logger.debug("Fetching feedback for user: {}", userId);
        return feedbackRepository.findByUserIdAndNotDeleted(userId, pageable)
                .map(feedbackMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackResponse> getMessageFeedback(UUID messageId, Pageable pageable) {
        logger.debug("Fetching feedback for message: {}", messageId);

        ChatMessage message = messageRepository.findByIdAndNotDeleted(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        return feedbackRepository.findByMessageIdAndNotDeleted(messageId, pageable)
                .map(feedbackMapper::toResponse);
    }

    @Override
    public void deleteFeedback(UUID feedbackId, UUID userId) {
        logger.info("Deleting feedback: {} for user: {}", feedbackId, userId);

        MessageFeedback feedback = feedbackRepository.findByIdAndNotDeleted(feedbackId)
                .orElseThrow(() -> new MessageNotFoundException("Feedback not found: " + feedbackId));

        if (!feedback.getUserId().equals(userId)) {
            throw new InvalidSessionException("Feedback does not belong to user");
        }

        feedbackRepository.softDelete(feedbackId, Instant.now());
        logger.info("Deleted feedback: {}", feedbackId);
    }
}
