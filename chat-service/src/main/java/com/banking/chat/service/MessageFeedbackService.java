package com.banking.chat.service;

import com.banking.chat.dto.FeedbackResponse;
import com.banking.chat.dto.SubmitFeedbackRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageFeedbackService {

    FeedbackResponse submitFeedback(UUID userId, SubmitFeedbackRequest request);

    FeedbackResponse getFeedback(UUID feedbackId, UUID userId);

    Page<FeedbackResponse> getUserFeedback(UUID userId, Pageable pageable);

    Page<FeedbackResponse> getMessageFeedback(UUID messageId, Pageable pageable);

    void deleteFeedback(UUID feedbackId, UUID userId);
}
