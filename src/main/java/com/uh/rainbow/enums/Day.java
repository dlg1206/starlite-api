package com.uh.rainbow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * <b>File:</b> Day.java
 * <p>
 * <b>Description:</b> Representation of a day of the week
 *
 * @author Derek Garcia
 */
public enum Day {
    SUN, MON, TUE, WED, THU, FRI, SAT, TBD;

    /**
     * Create day from string regardless of case
     *
     * @param value Value to convert into enum
     * @return Day Enum
     */
    @JsonCreator
    public static Day fromString(String value) {
        return Day.valueOf(value.toUpperCase());
    }

    /**
     * Convert string to Day enum
     * Supports UH day codes
     *
     * @param code string code ID
     * @return Day Enum, null if not
     */
    public static Day fromDayString(String code) {
        return switch (code.toLowerCase()) {
            case "sun", "sunday", "u" -> SUN;
            case "mon", "monday", "m" -> MON;
            case "tue", "tuesday", "t" -> TUE;
            case "wed", "wednesday", "w" -> WED;
            case "thu", "thursday", "r" -> THU;
            case "fri", "friday", "f" -> FRI;
            case "sat", "saturday", "s" -> SAT;
            case "tba" -> TBD;
            default -> null;
        };
    }
}
