package com.uh.rainbow.dto;

/**
 * Grading option for a course
 *
 * @param code        Banner grading code
 * @param description Description of grading option
 */
public record GradingOption(String code, String description) {
    @Override
    public boolean equals(Object o) {
        return o instanceof GradingOption other && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
