package com.banking.stt.util;

import com.banking.stt.domain.TranscriptionSegment;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * Utility class for formatting transcription text.
 * Provides methods for formatting transcripts with timestamps and speakers.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
public final class TranscriptFormatter {

    private TranscriptFormatter() {
        // Utility class
    }

    /**
     * Format transcription segments as plain text.
     *
     * @param segments List of segments
     * @return Formatted text
     */
    public static String formatAsPlainText(List<TranscriptionSegment> segments) {
        StringBuilder sb = new StringBuilder();

        for (TranscriptionSegment segment : segments) {
            sb.append(segment.getText().trim()).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Format transcription segments with timestamps.
     *
     * @param segments List of segments
     * @return Formatted text with timestamps
     */
    public static String formatWithTimestamps(List<TranscriptionSegment> segments) {
        StringBuilder sb = new StringBuilder();

        for (TranscriptionSegment segment : segments) {
            sb.append("[").append(segment.getFormattedTimeRange()).append("] ");
            sb.append(segment.getText().trim()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Format transcription segments with speakers.
     *
     * @param segments List of segments
     * @return Formatted text with speaker labels
     */
    public static String formatWithSpeakers(List<TranscriptionSegment> segments) {
        StringBuilder sb = new StringBuilder();

        for (TranscriptionSegment segment : segments) {
            if (segment.hasSpeaker()) {
                sb.append(segment.getSpeakerId()).append(": ");
            }
            sb.append(segment.getText().trim()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Format transcription segments with timestamps and speakers.
     *
     * @param segments List of segments
     * @return Formatted text with timestamps and speaker labels
     */
    public static String formatWithTimestampsAndSpeakers(List<TranscriptionSegment> segments) {
        StringBuilder sb = new StringBuilder();

        for (TranscriptionSegment segment : segments) {
            sb.append("[").append(segment.getFormattedTimeRange()).append("] ");

            if (segment.hasSpeaker()) {
                sb.append(segment.getSpeakerId()).append(": ");
            }

            sb.append(segment.getText().trim()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Format time in seconds to MM:SS format.
     *
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTime(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Format time in seconds to HH:MM:SS format.
     *
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTimeLong(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Format time in seconds to SRT subtitle format (HH:MM:SS,mmm).
     *
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTimeSrt(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        int millis = seconds.subtract(BigDecimal.valueOf(totalSeconds))
                .multiply(BigDecimal.valueOf(1000))
                .intValue();

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }

    /**
     * Format time in seconds to VTT subtitle format (HH:MM:SS.mmm).
     *
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTimeVtt(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        int millis = seconds.subtract(BigDecimal.valueOf(totalSeconds))
                .multiply(BigDecimal.valueOf(1000))
                .intValue();

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, secs, millis);
    }

    /**
     * Clean and normalize transcription text.
     *
     * @param text Raw transcription text
     * @return Cleaned text
     */
    public static String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Remove extra whitespace
        text = text.replaceAll("\\s+", " ");

        // Trim
        text = text.trim();

        // Capitalize first letter
        if (!text.isEmpty()) {
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        return text;
    }

    /**
     * Add punctuation to transcription text (basic heuristics).
     *
     * @param text Transcription text
     * @return Text with punctuation
     */
    public static String addPunctuation(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Basic punctuation rules
        text = text.replaceAll("\\s+", " ").trim();

        // Add period at the end if missing
        if (!text.matches(".*[.!?]$")) {
            text += ".";
        }

        return text;
    }
}
