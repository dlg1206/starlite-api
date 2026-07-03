package com.uh.rainbow.entities;

import java.util.Objects;
import java.util.Random;

/**
 * Create new composite course ID
 *
 * @param subjectCode Subject code of course
 * @param number      Course number
 */
public record CourseID(String subjectCode, String number) {

    /**
     * Generate placeholder ID for {@link TimeBuffer}
     *
     * @return Unique placeholder ID
     */
    public static CourseID generatePlaceholder() {
        return new CourseID("PLACEHOLDER", Integer.toString(new Random().nextInt()));
    }

    /**
     * @return True if course number contains wildcard, false otherwise
     */
    public boolean containsWildcard() {
        return number.contains("*");
    }

    @Override
    public String toString() {
        return "%s %s".formatted(subjectCode, number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseID(String code, String number1))) return false;
        return Objects.equals(subjectCode, code) &&
                Objects.equals(number, number1);
    }

}