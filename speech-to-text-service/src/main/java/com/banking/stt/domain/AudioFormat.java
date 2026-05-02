package com.banking.stt.domain;

/**
 * Enumeration of supported audio formats.
 * Represents the various audio file formats that can be processed by the service.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public enum AudioFormat {
    /**
     * MP3 format - MPEG Audio Layer III
     */
    MP3("audio/mpeg", ".mp3"),

    /**
     * WAV format - Waveform Audio File Format
     */
    WAV("audio/wav", ".wav"),

    /**
     * M4A format - MPEG-4 Audio
     */
    M4A("audio/mp4", ".m4a"),

    /**
     * FLAC format - Free Lossless Audio Codec
     */
    FLAC("audio/flac", ".flac"),

    /**
     * OGG format - Ogg Vorbis
     */
    OGG("audio/ogg", ".ogg"),

    /**
     * WEBM format - WebM Audio
     */
    WEBM("audio/webm", ".webm");

    private final String mimeType;
    private final String extension;

    AudioFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Get AudioFormat from file extension.
     *
     * @param extension File extension (with or without dot)
     * @return AudioFormat enum value
     * @throws IllegalArgumentException if format is not supported
     */
    public static AudioFormat fromExtension(String extension) {
        String normalizedExt = extension.toLowerCase();
        if (!normalizedExt.startsWith(".")) {
            normalizedExt = "." + normalizedExt;
        }

        for (AudioFormat format : values()) {
            if (format.extension.equals(normalizedExt)) {
                return format;
            }
        }

        throw new IllegalArgumentException("Unsupported audio format: " + extension);
    }

    /**
     * Get AudioFormat from MIME type.
     *
     * @param mimeType MIME type string
     * @return AudioFormat enum value
     * @throws IllegalArgumentException if format is not supported
     */
    public static AudioFormat fromMimeType(String mimeType) {
        for (AudioFormat format : values()) {
            if (format.mimeType.equalsIgnoreCase(mimeType)) {
                return format;
            }
        }

        throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
    }

    /**
     * Check if the given extension is supported.
     *
     * @param extension File extension
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(String extension) {
        try {
            fromExtension(extension);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
