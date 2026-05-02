# Feature 19: Speech-to-Text Service - Implementation Plan

**Date**: May 2, 2026  
**Status**: In Progress  
**Estimated Files**: 60+ files  
**Estimated Time**: 20-30 hours

---

## 🎯 OVERVIEW

Speech-to-Text Service provides audio transcription and speech recognition capabilities for the banking platform, enabling voice banking features and multimodal interaction.

### Core Capabilities
1. **Audio File Upload** - Upload audio files for batch transcription
2. **Real-time Transcription** - WebSocket-based streaming speech recognition
3. **Multi-language Support** - Support for 10+ languages
4. **Speaker Diarization** - Identify and separate multiple speakers
5. **Transcript Export** - Export transcripts in PDF, TXT, JSON formats
6. **Audio Format Conversion** - Convert various audio formats to Whisper-compatible format
7. **Integration with Chat Service** - Route transcripts to chat for AI processing

---

## 📋 COMPLETE FILE LIST (60 FILES)

### Core Application (1 file)
1. ✅ SpeechToTextApplication.java

### Domain Entities (6 files)
2. ⏳ AudioFile.java
3. ⏳ Transcription.java
4. ⏳ TranscriptionSegment.java
5. ⏳ SpeakerInfo.java
6. ⏳ AudioFormat.java (enum)
7. ⏳ TranscriptionStatus.java (enum)

### Repositories (3 files)
8. ⏳ AudioFileRepository.java
9. ⏳ TranscriptionRepository.java
10. ⏳ TranscriptionSegmentRepository.java

### DTOs (12 files)
11. ⏳ ApiResponse.java
12. ⏳ AudioUploadRequest.java
13. ⏳ AudioUploadResponse.java
14. ⏳ TranscriptionRequest.java
15. ⏳ TranscriptionResponse.java
16. ⏳ TranscriptionSegmentResponse.java
17. ⏳ SpeakerInfoResponse.java
18. ⏳ TranscriptionStatusResponse.java
19. ⏳ TranscriptionExportRequest.java
20. ⏳ RealtimeTranscriptionMessage.java
21. ⏳ WebSocketMessage.java
22. ⏳ LanguageDetectionResponse.java

### Configuration (8 files)
23. ⏳ SttProperties.java
24. ⏳ SecurityConfig.java
25. ⏳ RedisConfig.java
26. ⏳ KafkaConfig.java
27. ⏳ WebSocketConfig.java
28. ⏳ AsyncConfig.java
29. ⏳ OpenApiConfig.java
30. ⏳ WhisperClientConfig.java

### Service Interfaces (6 files)
31. ⏳ SpeechToTextService.java
32. ⏳ AudioProcessingService.java
33. ⏳ TranscriptionService.java
34. ⏳ SpeakerDiarizationService.java
35. ⏳ LanguageDetectionService.java
36. ⏳ ExportService.java

### Service Implementations (6 files)
37. ⏳ SpeechToTextServiceImpl.java
38. ⏳ AudioProcessingServiceImpl.java
39. ⏳ WhisperTranscriptionServiceImpl.java
40. ⏳ SimpleSpeakerDiarizationServiceImpl.java
41. ⏳ LanguageDetectionServiceImpl.java
42. ⏳ ExportServiceImpl.java

### Controllers (2 files)
43. ⏳ SpeechToTextController.java
44. ⏳ RealtimeTranscriptionController.java (WebSocket)

### Exception Handling (7 files)
45. ⏳ SttException.java
46. ⏳ AudioFileNotFoundException.java
47. ⏳ UnsupportedAudioFormatException.java
48. ⏳ TranscriptionFailedException.java
49. ⏳ InvalidAudioFileException.java
50. ⏳ TranscriptionNotFoundException.java
51. ⏳ GlobalExceptionHandler.java

### Events & Kafka (5 files)
52. ⏳ AudioUploadedEvent.java
53. ⏳ TranscriptionCompletedEvent.java
54. ⏳ TranscriptionFailedEvent.java
55. ⏳ SttEventPublisher.java
56. ⏳ TranscriptionConsumer.java

### Security (2 files)
57. ⏳ JwtUtil.java
58. ⏳ JwtAuthenticationFilter.java

### Utilities (3 files)
59. ⏳ AudioUtil.java
60. ⏳ TranscriptFormatter.java
61. ⏳ LanguageCodeMapper.java

### Mapper (1 file)
62. ⏳ TranscriptionMapper.java

### Database Migrations (5 files)
63. ⏳ changelog-master.xml
64. ⏳ V001__create_audio_files.sql
65. ⏳ V002__create_transcriptions.sql
66. ⏳ V003__create_transcription_segments.sql
67. ⏳ V004__create_indexes.sql
68. ⏳ V005__seed_supported_languages.sql

### Configuration Files (5 files)
69. ⏳ application.yml
70. ⏳ application-dev.yml
71. ⏳ application-staging.yml
72. ⏳ application-prod.yml
73. ⏳ logback-spring.xml

### Deployment (5 files)
74. ⏳ Dockerfile
75. ⏳ k8s/configmap.yaml
76. ⏳ k8s/deployment.yaml
77. ⏳ k8s/service.yaml
78. ⏳ k8s/hpa.yaml

### Documentation (3 files)
79. ⏳ README.md
80. ⏳ .env.example
81. ⏳ FEATURE_19_COMPLETION_SUMMARY.md

---

## 🏗️ ARCHITECTURE

### Technology Stack
- **Speech Recognition**: OpenAI Whisper API
- **Audio Processing**: JAVE (Java Audio Video Encoder)
- **Real-time Communication**: WebSocket (STOMP)
- **Storage**: PostgreSQL + File System/S3
- **Caching**: Redis
- **Events**: Kafka

### Supported Audio Formats
- MP3
- WAV
- M4A
- FLAC
- OGG
- WEBM

### Supported Languages
- English (en)
- Spanish (es)
- French (fr)
- German (de)
- Italian (it)
- Portuguese (pt)
- Dutch (nl)
- Russian (ru)
- Chinese (zh)
- Japanese (ja)
- Korean (ko)
- Hindi (hi)
- Arabic (ar)

---

## 🔄 WORKFLOW

### Batch Transcription Flow
1. User uploads audio file via REST API
2. Service validates file format and size
3. Audio converted to Whisper-compatible format (if needed)
4. File stored in object storage
5. Transcription job queued (Kafka event)
6. Whisper API processes audio
7. Transcription result stored in database
8. Completion event published
9. User retrieves transcript via API

### Real-time Transcription Flow
1. User connects via WebSocket
2. Authentication via JWT
3. Audio chunks streamed from client
4. Chunks buffered and processed
5. Partial transcripts sent back to client
6. Final transcript stored in database
7. WebSocket connection closed

---

## 📊 DATABASE SCHEMA

### audio_files
- id (UUID, PK)
- user_id (UUID, FK)
- filename (VARCHAR)
- original_format (VARCHAR)
- converted_format (VARCHAR)
- file_size_bytes (BIGINT)
- duration_seconds (DECIMAL)
- storage_path (VARCHAR)
- language_code (VARCHAR)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)

### transcriptions
- id (UUID, PK)
- audio_file_id (UUID, FK)
- user_id (UUID, FK)
- status (VARCHAR)
- language_detected (VARCHAR)
- confidence_score (DECIMAL)
- full_text (TEXT)
- word_count (INTEGER)
- processing_time_ms (BIGINT)
- model_used (VARCHAR)
- created_at (TIMESTAMPTZ)
- updated_at (TIMESTAMPTZ)

### transcription_segments
- id (UUID, PK)
- transcription_id (UUID, FK)
- segment_index (INTEGER)
- start_time_seconds (DECIMAL)
- end_time_seconds (DECIMAL)
- text (TEXT)
- speaker_id (VARCHAR)
- confidence_score (DECIMAL)
- created_at (TIMESTAMPTZ)

---

## 🔌 API ENDPOINTS

### REST API (8 endpoints)
1. `POST /v1/stt/upload` - Upload audio file
2. `GET /v1/stt/audio/{id}` - Get audio file details
3. `GET /v1/stt/transcriptions/{id}` - Get transcription
4. `GET /v1/stt/transcriptions/{id}/segments` - Get segments
5. `GET /v1/stt/transcriptions` - List transcriptions
6. `POST /v1/stt/transcriptions/{id}/export` - Export transcript
7. `DELETE /v1/stt/audio/{id}` - Delete audio file
8. `GET /v1/stt/languages` - Get supported languages

### WebSocket API (1 endpoint)
9. `WS /v1/stt/realtime` - Real-time transcription

---

## 🎯 KEY FEATURES

### Audio Processing
- Format conversion (MP3, WAV, M4A → Whisper format)
- Audio normalization
- Noise reduction (optional)
- Sample rate conversion
- Channel mixing (stereo → mono)

### Transcription
- Batch processing via Whisper API
- Real-time streaming transcription
- Language auto-detection
- Confidence scoring
- Timestamp alignment

### Speaker Diarization
- Speaker identification
- Speaker separation
- Speaker labeling (Speaker 1, Speaker 2, etc.)
- Speaker timeline

### Export Formats
- **PDF**: Formatted transcript with timestamps
- **TXT**: Plain text transcript
- **JSON**: Structured data with segments and metadata
- **SRT**: Subtitle format with timestamps
- **VTT**: WebVTT format for video captions

---

## 🔐 SECURITY

- JWT authentication on all endpoints
- Role-based access control (USER, ADMIN)
- File size limits (max 25MB)
- Audio duration limits (max 30 minutes)
- Rate limiting (10 uploads per hour per user)
- Secure file storage with access control
- PII detection and masking in transcripts

---

## 📈 OBSERVABILITY

### Metrics
- `stt.uploads.total` - Total audio uploads
- `stt.transcriptions.completed` - Completed transcriptions
- `stt.transcriptions.failed` - Failed transcriptions
- `stt.processing.duration` - Processing time
- `stt.whisper.api.calls` - Whisper API calls
- `stt.websocket.connections` - Active WebSocket connections

### Logging
- Audio upload events
- Transcription start/complete/fail
- Whisper API calls with latency
- WebSocket connection lifecycle
- Error events with context

---

## 🚀 DEPLOYMENT

### Docker
- Multi-stage build
- FFmpeg included for audio processing
- Non-root user
- Health checks

### Kubernetes
- 3 replicas (production)
- HPA based on CPU and memory
- Liveness and readiness probes
- ConfigMap for configuration
- Secrets for API keys

---

## ✅ NEXT STEPS

1. Create all domain entities and repositories
2. Implement service layer with Whisper integration
3. Build REST controllers
4. Implement WebSocket controller for real-time
5. Create Kafka events and consumers
6. Add database migrations
7. Create configuration files
8. Build Docker image
9. Create Kubernetes manifests
10. Write comprehensive documentation

---

**Status**: Ready to begin implementation  
**Approach**: Systematic file-by-file creation  
**Quality**: Production-grade, fully tested, documented

