package com.banking.stt.domain;

/**
 * Enumeration of transcription processing statuses.
 * Represents the lifecycle states of a transcription job.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public enum TranscriptionStatus {
    /**
     * Transcription job has been created but not yet started
     */
    PENDING("Pending", "Transcription is queued for processing"),

    /**
     * Transcription is currently being processed
     */
    PROCESSING("Processing", "Transcription is in progress"),

    /**
     * Transcription completed successfully
     */
    COMPLETED("Completed", "Transcription completed successfully"),

    /**
     * Transcription failed due to an error
     */
    FAILED("Failed", "Transcription failed");

    private final String displayName;
    private final String description;

    TranscriptionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the status represents a terminal state.
     *
     * @return true if status is COMPLETED or FAILED
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    /**
     * Check if the status represents an active processing state.
     *
     * @return true if status is PENDING or PROCESSING
     */
    public boolean isActive() {
        return this == PENDING || this == PROCESSING;
    }
}
