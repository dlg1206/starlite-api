package com.uh.starlite.enums;

import lombok.Getter;

/**
 * <b>File:</b> GradingOption.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
@Getter
public enum GradingOption {
    GRADE("G", "Letter Plus + Minus"),
    PASS_FAIL("C", "Credit/No Credit"),
    AUDIT("A", "Audit");

    private final String code;
    private final String description;

    /**
     * Grading option for a course
     *
     * @param code        Banner grading code
     * @param description Description of grading option
     */
    GradingOption(String code, String description) {
        this.code = code;
        this.description = description;
    }


    /**
     * Create Grading Option from code string regardless of case
     *
     * @param code Code to convert into enum
     * @return Grading option enum
     */
    public static GradingOption fromCode(String code) {
        if (code == null)
            throw new IllegalArgumentException("Grading option code must not be null");
        return switch (code.toLowerCase()) {
            case "g" -> GRADE;
            case "c" -> PASS_FAIL;
            case "a" -> AUDIT;
            default -> throw new IllegalArgumentException("Unknown grading code: " + code);
        };
    }
}
