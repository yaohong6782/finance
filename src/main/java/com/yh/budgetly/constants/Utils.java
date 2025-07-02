package com.yh.budgetly.constants;

public class Utils {
    public static String sanitizeFileName(String fileName) {
        // Replace spaces, non-breaking spaces, narrow spaces with underscores
        String cleaned = fileName.replaceAll("[\\s\u00A0\u202F]+", "_");

        // Remove non-ASCII characters
        cleaned = cleaned.replaceAll("[^\\x20-\\x7E]", "");

        // Replace illegal file path characters (Windows-safe too)
        cleaned = cleaned.replaceAll("[\\\\/:*?\"<>|]", "_");

        return cleaned;
    }
}
