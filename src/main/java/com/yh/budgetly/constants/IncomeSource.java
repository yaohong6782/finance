package com.yh.budgetly.constants;

import lombok.extern.slf4j.Slf4j;

public enum IncomeSource {
    CORPORATE_JOB,
    FREELANCE,
    INVESTMENTS,
    STOCKS,
    OTHER;

    public static IncomeSource fromString(String source) {
        if (source == null)  return null;
        String label = source.trim().toUpperCase().replace(" ", "_");

        return switch (label) {
            case "CORPORATE_JOB" -> CORPORATE_JOB;
            case "FREELANCE" -> FREELANCE;
            case "STOCKS" -> STOCKS;
            case "INVESTMENTS" -> INVESTMENTS;
            case "OTHER" -> OTHER;
            default -> null;
        };
    }
}
