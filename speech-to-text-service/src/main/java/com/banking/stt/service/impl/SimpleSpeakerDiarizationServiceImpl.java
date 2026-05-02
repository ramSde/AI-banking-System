package com.banking.stt.service.impl;

import com.banking.stt.domain.TranscriptionSegment;
import com.banking.stt.dto.SpeakerInfoResponse;
import com.banking.stt.repository.TranscriptionSegmentRepository;
import com.banking.stt.service.SpeakerDiarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple implementation of speaker diarization service.
 * Uses basic heuristics for speaker separation.
 * In production, this would integrate with advanced diarization models.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleSpeakerDiarizationServiceImpl implements SpeakerDiarizationService {

    private final TranscriptionSegmentRepository segmentRepository;

    @Override
    public List<TranscriptionSegment> performDiarization(UUID transcriptionId,
                                                         List<TranscriptionSegment> segments,
                                                         Integer expectedSpeakers) {
        log.info("Performing speaker diarization for transcription: {}", transcriptionId);

        if (segments == null || segments.isEmpty()) {
            log.warn("No segments to diarize");
            return segments;
        }

        int numSpeakers = expectedSpeakers != null ? expectedSpeakers : 2;

        // Simple alternating speaker assignment
        // In production, use advanced diarization models like pyannote.audio
        for (int i = 0; i < segments.size(); i++) {
            TranscriptionSegment segment = segments.get(i);
            String speakerId = "Speaker " + ((i % numSpeakers) + 1);
            segment.setSpeakerId(speakerId);
        }

        // Save updated segments
        segmentRepository.saveAll(segments);

        log.info("Diarization completed: {} speakers assigned to {} segments",
                numSpeakers, segments.size());

        return segments;
    }

    @Override
    public List<SpeakerInfoResponse> getSpeakerInfo(UUID transcriptionId) {
        log.info("Getting speaker info for transcription: {}", transcriptionId);

        List<TranscriptionSegment> segments = segmentRepository
                .findByTranscriptionIdOrderBySegmentIndexAsc(transcriptionId);

        if (segments.isEmpty()) {
            return Collections.emptyList();
        }

        // Group segments by speaker
        Map<String, List<TranscriptionSegment>> segmentsBySpeaker = segments.stream()
                .filter(TranscriptionSegment::hasSpeaker)
                .collect(Collectors.groupingBy(TranscriptionSegment::getSpeakerId));

        // Build speaker info
        List<SpeakerInfoResponse> speakerInfoList = new ArrayList<>();

        for (Map.Entry<String, List<TranscriptionSegment>> entry : segmentsBySpeaker.entrySet()) {
            String speakerId = entry.getKey();
            List<TranscriptionSegment> speakerSegments = entry.getValue();

            // Calculate total speaking time
            BigDecimal totalSpeakingTime = speakerSegments.stream()
                    .map(TranscriptionSegment::getDurationSeconds)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate word count
            int wordCount = speakerSegments.stream()
                    .mapToInt(TranscriptionSegment::getWordCount)
                    .sum();

            SpeakerInfoResponse speakerInfo = SpeakerInfoResponse.builder()
                    .speakerId(speakerId)
                    .segmentCount(speakerSegments.size())
                    .totalSpeakingTimeSeconds(totalSpeakingTime.doubleValue())
                    .wordCount(wordCount)
                    .build();

            speakerInfoList.add(speakerInfo);
        }

        // Sort by speaking time (descending)
        speakerInfoList.sort((a, b) ->
                Double.compare(b.getTotalSpeakingTimeSeconds(), a.getTotalSpeakingTimeSeconds()));

        log.info("Found {} speakers", speakerInfoList.size());
        return speakerInfoList;
    }

    @Override
    public int countSpeakers(UUID transcriptionId) {
        return segmentRepository.countDistinctSpeakersByTranscriptionId(transcriptionId);
    }
}
