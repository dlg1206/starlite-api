package com.uh.starlite.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

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
     * Get a list that includes one enum of each day
     *
     * @return All days in a week
     */
    public static List<Day> getWeek() {
        return List.of(SUN, MON, TUE, WED, THU, FRI, SAT);
    }

    /**
     * Create day from string regardless of case
     *
     * @param value Value to convert into enum
     * @return Day Enum
     */
    @JsonCreator
    public static Day fromDayString(String value) {
        if (value == null)
            throw new IllegalArgumentException("Day value must not be null");
        return switch (value.toLowerCase()) {
            case "sun", "sunday", "u" -> SUN;
            case "mon", "monday", "m" -> MON;
            case "tue", "tuesday", "t" -> TUE;
            case "wed", "wednesday", "w" -> WED;
            case "thu", "thursday", "r" -> THU;
            case "fri", "friday", "f" -> FRI;
            case "sat", "saturday", "s" -> SAT;
            case "tba" -> TBD;
            default -> throw new IllegalArgumentException("Unknown day code: " + value);
        };
    }
}
