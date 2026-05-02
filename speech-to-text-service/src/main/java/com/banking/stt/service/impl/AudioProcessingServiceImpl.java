package com.banking.stt.service.impl;

import com.banking.stt.config.SttProperties;
import com.banking.stt.domain.AudioFile;
import com.banking.stt.domain.AudioFormat;
import com.banking.stt.dto.AudioUploadRequest;
import com.banking.stt.exception.InvalidAudioFileException;
import com.banking.stt.exception.UnsupportedAudioFormatException;
import com.banking.stt.repository.AudioFileRepository;
import com.banking.stt.service.AudioProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implementation of audio processing service.
 * Handles audio format conversion, validation, and storage using JAVE (FFmpeg wrapper).
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AudioProcessingServiceImpl implements AudioProcessingService {

    private final AudioFileRepository audioFileRepository;
    private final SttProperties sttProperties;

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25MB
    private static final int MAX_DURATION_SECONDS = 30 * 60; // 30 minutes

    @Override
    public AudioFile processAndStore(MultipartFile file, AudioUploadRequest request, UUID userId) {
        log.info("Processing audio file: {} for user: {}", file.getOriginalFilename(), userId);

        try {
            // 1. Validate file
            validateFile(file);

            // 2. Save to temporary location
            File tempFile = saveTempFile(file);

            // 3. Get audio metadata
            MultimediaInfo audioInfo = getAudioInfo(tempFile);
            double durationSeconds = audioInfo.getDuration() / 1000.0;

            // 4. Validate duration
            if (durationSeconds > MAX_DURATION_SECONDS) {
                throw new InvalidAudioFileException(
                        String.format("Audio duration %.2f seconds exceeds maximum %d seconds",
                                durationSeconds, MAX_DURATION_SECONDS)
                );
            }

            // 5. Determine format
            AudioFormat originalFormat = detectAudioFormat(file.getOriginalFilename());

            // 6. Convert if necessary
            File processedFile = tempFile;
            AudioFormat convertedFormat = null;
            if (needsConversion(originalFormat)) {
                File convertedFile = convertToWhisperFormat(tempFile);
                processedFile = convertedFile;
                convertedFormat = AudioFormat.WAV;
            }

            // 7. Store in object storage (simulated with local storage for now)
            String storagePath = storeFile(processedFile, userId);

            // 8. Create AudioFile entity
            AudioFile audioFile = AudioFile.builder()
                    .userId(userId)
                    .filename(file.getOriginalFilename())
                    .originalFormat(originalFormat)
                    .convertedFormat(convertedFormat)
                    .fileSizeBytes(file.getSize())
                    .durationSeconds(BigDecimal.valueOf(durationSeconds))
                    .storagePath(storagePath)
                    .languageCode(request.getLanguageCode())
                    .build();

            // 9. Save to database
            AudioFile savedAudioFile = audioFileRepository.save(audioFile);

            // 10. Cleanup temp files
            cleanupTempFile(tempFile);
            if (processedFile != tempFile) {
                cleanupTempFile(processedFile);
            }

            log.info("Audio file processed successfully: {}", savedAudioFile.getId());
            return savedAudioFile;

        } catch (IOException | EncoderException e) {
            log.error("Failed to process audio file", e);
            throw new InvalidAudioFileException("Failed to process audio file: " + e.getMessage());
        }
    }

    @Override
    public boolean convertAudioFormat(File sourceFile, File targetFile) {
        try {
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("pcm_s16le");
            audio.setBitRate(256000);
            audio.setChannels(1);
            audio.setSamplingRate(16000);

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("wav");
            attrs.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(sourceFile), targetFile, attrs);

            log.info("Audio converted successfully: {} -> {}", sourceFile.getName(), targetFile.getName());
            return true;

        } catch (EncoderException e) {
            log.error("Failed to convert audio format", e);
            return false;
        }
    }

    @Override
    public Double getAudioDuration(File file) {
        try {
            MultimediaObject multimediaObject = new MultimediaObject(file);
            MultimediaInfo info = multimediaObject.getInfo();
            return info.getDuration() / 1000.0; // Convert to seconds
        } catch (EncoderException e) {
            log.error("Failed to get audio duration", e);
            return null;
        }
    }

    @Override
    public boolean validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        AudioFormat format = detectAudioFormat(filename);
        return format != null;
    }

    @Override
    public File downloadAudioFile(UUID audioFileId) {
        AudioFile audioFile = audioFileRepository.findById(audioFileId)
                .orElseThrow(() -> new InvalidAudioFileException("Audio file not found"));

        // In production, download from S3/MinIO
        // For now, return file from local storage
        File file = new File(audioFile.getStoragePath());
        if (!file.exists()) {
            throw new InvalidAudioFileException("Audio file not found in storage");
        }

        return file;
    }

    @Override
    public void deleteFromStorage(String storagePath) {
        try {
            Path path = Paths.get(storagePath);
            Files.deleteIfExists(path);
            log.info("Deleted file from storage: {}", storagePath);
        } catch (IOException e) {
            log.error("Failed to delete file from storage: {}", storagePath, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAudioFileException("Audio file is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidAudioFileException(
                    String.format("File size %.2f MB exceeds maximum 25 MB",
                            file.getSize() / (1024.0 * 1024.0))
            );
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new InvalidAudioFileException("Filename is empty");
        }
    }

    private File saveTempFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path tempPath = Paths.get(TEMP_DIR, filename);
        Files.write(tempPath, file.getBytes());
        return tempPath.toFile();
    }

    private MultimediaInfo getAudioInfo(File file) {
        try {
            MultimediaObject multimediaObject = new MultimediaObject(file);
            return multimediaObject.getInfo();
        } catch (EncoderException e) {
            throw new InvalidAudioFileException("Failed to read audio file metadata: " + e.getMessage());
        }
    }

    private AudioFormat detectAudioFormat(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        try {
            return AudioFormat.fromExtension(extension);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedAudioFormatException(extension);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    private boolean needsConversion(AudioFormat format) {
        // Whisper API accepts various formats, but WAV 16kHz mono is optimal
        return format != AudioFormat.WAV;
    }

    private File convertToWhisperFormat(File sourceFile) throws EncoderException {
        String targetFilename = UUID.randomUUID() + "_converted.wav";
        File targetFile = new File(TEMP_DIR, targetFilename);

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        audio.setBitRate(256000);
        audio.setChannels(1);
        audio.setSamplingRate(16000);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(sourceFile), targetFile, attrs);

        log.info("Audio converted to Whisper format: {}", targetFile.getName());
        return targetFile;
    }

    private String storeFile(File file, UUID userId) throws IOException {
        // In production, upload to S3/MinIO
        // For now, store in local directory
        String storageDir = sttProperties.getStorage().getBucket();
        Path storagePath = Paths.get(storageDir, userId.toString());
        Files.createDirectories(storagePath);

        String storedFilename = UUID.randomUUID() + "_" + file.getName();
        Path targetPath = storagePath.resolve(storedFilename);
        Files.copy(file.toPath(), targetPath);

        log.info("File stored at: {}", targetPath);
        return targetPath.toString();
    }

    private void cleanupTempFile(File file) {
        try {
            if (file != null && file.exists()) {
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup temp file: {}", file.getPath(), e);
        }
    }
}
