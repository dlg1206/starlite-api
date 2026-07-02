package com.uh.rainbow.entities;

import java.util.Objects;

/**
 * Create new composite course ID
 *
 * @param subjectCode Subject code of course
 * @param number      Course number
 */
public record CourseID(String subjectCode, String number) {

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