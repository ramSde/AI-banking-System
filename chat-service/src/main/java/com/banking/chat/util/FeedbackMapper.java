package com.banking.chat.util;

import com.banking.chat.domain.MessageFeedback;
import com.banking.chat.dto.FeedbackResponse;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackResponse toResponse(MessageFeedback feedback) {
        if (feedback == null) {
            return null;
        }

        return new FeedbackResponse(
                feedback.getId(),
                feedback.getMessageId(),
                feedback.getUserId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getMetadata(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }
}
