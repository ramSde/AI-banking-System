package com.banking.stt.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping language codes and names.
 * Provides ISO 639-1 language code mappings.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public final class LanguageCodeMapper {

    private static final Map<String, String> CODE_TO_NAME = new HashMap<>();
    private static final Map<String, String> NAME_TO_CODE = new HashMap<>();

    static {
        // Initialize language mappings
        addLanguage("en", "English");
        addLanguage("es", "Spanish");
        addLanguage("fr", "French");
        addLanguage("de", "German");
        addLanguage("it", "Italian");
        addLanguage("pt", "Portuguese");
        addLanguage("nl", "Dutch");
        addLanguage("pl", "Polish");
        addLanguage("ru", "Russian");
        addLanguage("zh", "Chinese");
        addLanguage("ja", "Japanese");
        addLanguage("ko", "Korean");
        addLanguage("ar", "Arabic");
        addLanguage("hi", "Hindi");
        addLanguage("tr", "Turkish");
        addLanguage("vi", "Vietnamese");
        addLanguage("th", "Thai");
        addLanguage("id", "Indonesian");
        addLanguage("ms", "Malay");
        addLanguage("fil", "Filipino");
        addLanguage("sv", "Swedish");
        addLanguage("no", "Norwegian");
        addLanguage("da", "Danish");
        addLanguage("fi", "Finnish");
        addLanguage("el", "Greek");
        addLanguage("he", "Hebrew");
        addLanguage("cs", "Czech");
        addLanguage("ro", "Romanian");
        addLanguage("hu", "Hungarian");
        addLanguage("uk", "Ukrainian");
        addLanguage("bg", "Bulgarian");
        addLanguage("hr", "Croatian");
        addLanguage("sk", "Slovak");
        addLanguage("sl", "Slovenian");
        addLanguage("lt", "Lithuanian");
        addLanguage("lv", "Latvian");
        addLanguage("et", "Estonian");
        addLanguage("is", "Icelandic");
        addLanguage("ga", "Irish");
        addLanguage("mt", "Maltese");
        addLanguage("cy", "Welsh");
    }

    private LanguageCodeMapper() {
        // Utility class
    }

    private static void addLanguage(String code, String name) {
        CODE_TO_NAME.put(code.toLowerCase(), name);
        NAME_TO_CODE.put(name.toLowerCase(), code.toLowerCase());
    }

    /**
     * Get language name from code.
     *
     * @param code Language code (ISO 639-1)
     * @return Language name, or "Unknown" if not found
     */
    public static String getLanguageName(String code) {
        if (code == null) {
            return "Unknown";
        }
        return CODE_TO_NAME.getOrDefault(code.toLowerCase(), "Unknown");
    }

    /**
     * Get language code from name.
     *
     * @param name Language name
     * @return Language code, or null if not found
     */
    public static String getLanguageCode(String name) {
        if (name == null) {
            return null;
        }
        return NAME_TO_CODE.get(name.toLowerCase());
    }

    /**
     * Check if language code is valid.
     *
     * @param code Language code
     * @return true if valid
     */
    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        return CODE_TO_NAME.containsKey(code.toLowerCase());
    }

    /**
     * Check if language name is valid.
     *
     * @param name Language name
     * @return true if valid
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return NAME_TO_CODE.containsKey(name.toLowerCase());
    }

    /**
     * Normalize language code to lowercase.
     *
     * @param code Language code
     * @return Normalized code
     */
    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.toLowerCase();
    }

    /**
     * Get all supported language codes.
     *
     * @return Array of language codes
     */
    public static String[] getSupportedCodes() {
        return CODE_TO_NAME.keySet().toArray(new String[0]);
    }

    /**
     * Get all supported language names.
     *
     * @return Array of language names
     */
    public static String[] getSupportedNames() {
        return CODE_TO_NAME.values().toArray(new String[0]);
    }
}
