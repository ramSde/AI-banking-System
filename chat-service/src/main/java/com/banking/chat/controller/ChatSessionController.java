package com.banking.chat.controller;

import com.banking.chat.domain.ChatSession;
import com.banking.chat.dto.ApiResponse;
import com.banking.chat.dto.CreateSessionRequest;
import com.banking.chat.dto.SessionResponse;
import com.banking.chat.dto.UpdateSessionRequest;
import com.banking.chat.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/v1/chat/sessions")
@Tag(name = "Chat Sessions", description = "Chat session management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping
    @Operation(summary = "Create a new chat session", description = "Creates a new chat session for the authenticated user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Session created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateSessionRequest request) {
        SessionResponse response = chatSessionService.createSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session details", description = "Retrieves details of a specific chat session")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
        SessionResponse response = chatSessionService.getSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get user sessions", description = "Retrieves all chat sessions for the authenticated user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getUserSessions(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20, sort = "lastActivityAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SessionResponse> response = chatSessionService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get sessions by status", description = "Retrieves chat sessions filtered by status")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> getUserSessionsByStatus(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session status") @PathVariable ChatSession.SessionStatus status,
            @PageableDefault(size = 20, sort = "lastActivityAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SessionResponse> response = chatSessionService.getUserSessionsByStatus(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update session", description = "Updates a chat session")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<SessionResponse>> updateSession(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSessionRequest request) {
        SessionResponse response = chatSessionService.updateSession(sessionId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete session", description = "Deletes a chat session")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Session deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
        chatSessionService.deleteSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/archive")
    @Operation(summary = "Archive session", description = "Archives a chat session")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Session archived successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> archiveSession(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
        chatSessionService.archiveSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search sessions", description = "Searches chat sessions by title")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<SessionResponse>>> searchSessions(
            @AuthenticationPrincipal UUID userId,
            @Parameter(description = "Search term") @RequestParam String query,
            @PageableDefault(size = 20, sort = "lastActivityAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SessionResponse> response = chatSessionService.searchSessions(userId, query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
