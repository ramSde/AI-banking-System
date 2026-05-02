package com.banking.stt.service;

import com.banking.stt.domain.Transcription;
import com.banking.stt.domain.TranscriptionSegment;
import com.banking.stt.dto.TranscriptionExportRequest;

import java.util.List;

/**
 * Service interface for transcription export operations.
 * Exports transcriptions in various formats (PDF, TXT, JSON, SRT, VTT).
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface ExportService {

    /**
     * Export transcription in specified format.
     *
     * @param transcription Transcription entity
     * @param segments      List of transcription segments
     * @param request       Export request with format and options
     * @return Exported file as byte array
     */
    byte[] export(Transcription transcription, List<TranscriptionSegment> segments, TranscriptionExportRequest request);

    /**
     * Export as plain text.
     *
     * @param transcription Transcription entity
     * @param segments      List of segments
     * @param includeTimestamps Include timestamps in output
     * @return Text file as byte array
     */
    byte[] exportAsText(Transcription transcription, List<TranscriptionSegment> segments, boolean includeTimestamps);

    /**
     * Export as JSON.
     *
     * @param transcription Transcription entity
     * @param segments      List of segments
     * @return JSON file as byte array
     */
    byte[] exportAsJson(Transcription transcription, List<TranscriptionSegment> segments);

    /**
     * Export as PDF.
     *
     * @param transcription Transcription entity
     * @param segments      List of segments
     * @return PDF file as byte array
     */
    byte[] exportAsPdf(Transcription transcription, List<TranscriptionSegment> segments);

    /**
     * Export as SRT (SubRip subtitle format).
     *
     * @param segments List of segments
     * @return SRT file as byte array
     */
    byte[] exportAsSrt(List<TranscriptionSegment> segments);

    /**
     * Export as VTT (WebVTT subtitle format).
     *
     * @param segments List of segments
     * @return VTT file as byte array
     */
    byte[] exportAsVtt(List<TranscriptionSegment> segments);
}
