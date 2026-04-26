package com.banking.chat.controller;

import com.banking.chat.dto.*;
import com.banking.chat.service.ChatMessageService;
import com.banking.chat.service.MessageFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/chat")
@Tag(name = "Chat Messages", description = "Chat message and feedback endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final MessageFeedbackService messageFeedbackService;

    public ChatMessageController(
            ChatMessageService chatMessageService,
            MessageFeedbackService messageFeedbackService) {
        this.chatMessageService = chatMessageService;
        this.messageFeedbackService = messageFeedbackService;
    }

    @PostMapping("/messages")
    @Operation(summary = "Send a message", description = "Sends a message in a chat session and receives AI response")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = chatMessageService.sendMessage(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/messages/{messageId}")
    @Operation(summary = "Get message details", description = "Retrieves details of a specific message")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Message retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Message not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<MessageResponse>> getMessage(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Message ID") @PathVariable UUID messageId) {
        MessageResponse response = chatMessageService.getMessage(messageId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get session messages", description = "Retrieves all messages in a chat session")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getSessionMessages(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<MessageResponse> response = chatMessageService.getSessionMessages(sessionId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sessions/{sessionId}/history")
    @Operation(summary = "Get chat history", description = "Retrieves chat history with session and messages")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<ChatHistoryResponse>> getChatHistory(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Parameter(description = "Number of messages to retrieve") @RequestParam(required = false) Integer limit) {
        ChatHistoryResponse response = chatMessageService.getChatHistory(sessionId, userId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete message", description = "Deletes a specific message")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Message deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Message not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Message ID") @PathVariable UUID messageId) {
        chatMessageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/feedback")
    @Operation(summary = "Submit feedback", description = "Submits feedback for a message")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Feedback submitted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody SubmitFeedbackRequest request) {
        FeedbackResponse response = messageFeedbackService.submitFeedback(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/feedback/{feedbackId}")
    @Operation(summary = "Get feedback details", description = "Retrieves details of specific feedback")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Feedback retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Feedback not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Feedback ID") @PathVariable UUID feedbackId) {
        FeedbackResponse response = messageFeedbackService.getFeedback(feedbackId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/feedback")
    @Operation(summary = "Get user feedback", description = "Retrieves all feedback submitted by the user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Feedback retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getUserFeedback(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FeedbackResponse> response = messageFeedbackService.getUserFeedback(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/feedback/{feedbackId}")
    @Operation(summary = "Delete feedback", description = "Deletes specific feedback")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Feedback deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Feedback not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteFeedback(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Feedback ID") @PathVariable UUID feedbackId) {
        messageFeedbackService.deleteFeedback(feedbackId, userId);
        return ResponseEntity.noContent().build();
    }
}
