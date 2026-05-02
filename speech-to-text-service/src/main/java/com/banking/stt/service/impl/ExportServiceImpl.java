package com.banking.stt.service.impl;

import com.banking.stt.domain.Transcription;
import com.banking.stt.domain.TranscriptionSegment;
import com.banking.stt.dto.TranscriptionExportRequest;
import com.banking.stt.exception.InvalidAudioFileException;
import com.banking.stt.service.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of export service.
 * Exports transcriptions in various formats (PDF, TXT, JSON, SRT, VTT).
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ObjectMapper objectMapper;

    @Override
    public byte[] export(Transcription transcription, List<TranscriptionSegment> segments,
                         TranscriptionExportRequest request) {
        log.info("Exporting transcription {} as {}", transcription.getId(), request.getFormat());

        String format = request.getFormat().toLowerCase();

        return switch (format) {
            case "txt" -> exportAsText(transcription, segments, request.getIncludeTimestamps());
            case "json" -> exportAsJson(transcription, segments);
            case "pdf" -> exportAsPdf(transcription, segments);
            case "srt" -> exportAsSrt(segments);
            case "vtt" -> exportAsVtt(segments);
            default -> throw new InvalidAudioFileException("Unsupported export format: " + format);
        };
    }

    @Override
    public byte[] exportAsText(Transcription transcription, List<TranscriptionSegment> segments,
                                boolean includeTimestamps) {
        log.info("Exporting as plain text (timestamps: {})", includeTimestamps);

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Transcription Export\n");
        sb.append("===================\n\n");
        sb.append("Transcription ID: ").append(transcription.getId()).append("\n");
        sb.append("Language: ").append(transcription.getLanguageDetected()).append("\n");
        sb.append("Word Count: ").append(transcription.getWordCount()).append("\n");
        sb.append("Created: ").append(formatTimestamp(transcription.getCreatedAt().toString())).append("\n");
        sb.append("\n---\n\n");

        // Content
        if (includeTimestamps && !segments.isEmpty()) {
            for (TranscriptionSegment segment : segments) {
                sb.append("[").append(segment.getFormattedTimeRange()).append("]");
                if (segment.hasSpeaker()) {
                    sb.append(" ").append(segment.getSpeakerId()).append(":");
                }
                sb.append(" ").append(segment.getText()).append("\n\n");
            }
        } else {
            sb.append(transcription.getFullText()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportAsJson(Transcription transcription, List<TranscriptionSegment> segments) {
        log.info("Exporting as JSON");

        try {
            Map<String, Object> exportData = new HashMap<>();

            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("transcriptionId", transcription.getId());
            metadata.put("audioFileId", transcription.getAudioFileId());
            metadata.put("language", transcription.getLanguageDetected());
            metadata.put("wordCount", transcription.getWordCount());
            metadata.put("processingTimeMs", transcription.getProcessingTimeMs());
            metadata.put("modelUsed", transcription.getModelUsed());
            metadata.put("createdAt", transcription.getCreatedAt());
            metadata.put("completedAt", transcription.getUpdatedAt());

            exportData.put("metadata", metadata);
            exportData.put("fullText", transcription.getFullText());

            // Segments
            if (!segments.isEmpty()) {
                List<Map<String, Object>> segmentList = segments.stream()
                        .map(this::segmentToMap)
                        .toList();
                exportData.put("segments", segmentList);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            return mapper.writeValueAsBytes(exportData);

        } catch (IOException e) {
            log.error("Failed to export as JSON", e);
            throw new InvalidAudioFileException("Failed to export as JSON: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportAsPdf(Transcription transcription, List<TranscriptionSegment> segments) {
        log.info("Exporting as PDF");

        // In production, use a PDF library like iText or Apache PDFBox
        // For now, return text format with PDF header
        String pdfNote = "PDF Export (Note: Full PDF generation requires PDF library integration)\n\n";
        byte[] textContent = exportAsText(transcription, segments, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(pdfNote.getBytes(StandardCharsets.UTF_8));
            baos.write(textContent);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to export as PDF", e);
            throw new InvalidAudioFileException("Failed to export as PDF: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportAsSrt(List<TranscriptionSegment> segments) {
        log.info("Exporting as SRT subtitle format");

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            TranscriptionSegment segment = segments.get(i);

            // Sequence number
            sb.append(i + 1).append("\n");

            // Timestamp (HH:MM:SS,mmm --> HH:MM:SS,mmm)
            sb.append(formatSrtTime(segment.getStartTimeSeconds()))
                    .append(" --> ")
                    .append(formatSrtTime(segment.getEndTimeSeconds()))
                    .append("\n");

            // Text
            String text = segment.getText().trim();
            if (segment.hasSpeaker()) {
                text = segment.getSpeakerId() + ": " + text;
            }
            sb.append(text).append("\n\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportAsVtt(List<TranscriptionSegment> segments) {
        log.info("Exporting as VTT subtitle format");

        StringBuilder sb = new StringBuilder();

        // VTT header
        sb.append("WEBVTT\n\n");

        for (TranscriptionSegment segment : segments) {
            // Timestamp (HH:MM:SS.mmm --> HH:MM:SS.mmm)
            sb.append(formatVttTime(segment.getStartTimeSeconds()))
                    .append(" --> ")
                    .append(formatVttTime(segment.getEndTimeSeconds()))
                    .append("\n");

            // Text
            String text = segment.getText().trim();
            if (segment.hasSpeaker()) {
                sb.append("<v ").append(segment.getSpeakerId()).append(">");
                sb.append(text);
            } else {
                sb.append(text);
            }
            sb.append("\n\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Map<String, Object> segmentToMap(TranscriptionSegment segment) {
        Map<String, Object> map = new HashMap<>();
        map.put("index", segment.getSegmentIndex());
        map.put("startTime", segment.getStartTimeSeconds());
        map.put("endTime", segment.getEndTimeSeconds());
        map.put("duration", segment.getDurationSeconds());
        map.put("text", segment.getText());
        map.put("speakerId", segment.getSpeakerId());
        map.put("confidence", segment.getConfidenceScore());
        map.put("wordCount", segment.getWordCount());
        return map;
    }

    private String formatSrtTime(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        int millis = seconds.subtract(BigDecimal.valueOf(totalSeconds))
                .multiply(BigDecimal.valueOf(1000))
                .intValue();

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }

    private String formatVttTime(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        int millis = seconds.subtract(BigDecimal.valueOf(totalSeconds))
                .multiply(BigDecimal.valueOf(1000))
                .intValue();

        return String.format("%02d:%02d:%02d.%03d", hours, minutes, secs, millis);
    }

    private String formatTimestamp(String isoTimestamp) {
        try {
            return DateTimeFormatter.ISO_INSTANT.format(
                    DateTimeFormatter.ISO_INSTANT.parse(isoTimestamp)
            );
        } catch (Exception e) {
            return isoTimestamp;
        }
    }
}
