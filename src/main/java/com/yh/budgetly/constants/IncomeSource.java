package com.yh.budgetly.constants;


public enum IncomeSource {
    CORPORATE_JOB,
    FREELANCE,
    INVESTMENTS,
    OTHER;

    public static IncomeSource fromString(String source) {
        if (source == null)  return null;
        String label = source.trim().toUpperCase().replace(" ", "_");

        return switch (label) {
            case "CORPORATE_JOB" -> CORPORATE_JOB;
            case "FREELANCE" -> FREELANCE;
            case "INVESTMENTS" -> INVESTMENTS;
            case "OTHER" -> OTHER;
            default -> null;
        };
    }
}
