package com.uh.rainbow.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Day.java
 * <p>
 * <b>Description:</b> Day of Week Enum
 *
 * @author Derek Garcia
 */
public enum Day {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    TBA(7);    // Special "To be announced Day"

    private final int dow;

    /**
     * Create new day with a numeric value
     *
     * @param dow Day of Week
     */
    Day(int dow) {
        this.dow = dow;
    }

    /**
     * Convert UH Day code to Day enum
     *
     * @param code string code ID
     * @return Day Enum, null if not
     */
    public static Day toDay(String code) {
        return switch (code.toLowerCase()) {
            case "sunday", "u" -> SUNDAY;
            case "monday", "m" -> MONDAY;
            case "tuesday", "t" -> TUESDAY;
            case "wednesday", "w" -> WEDNESDAY;
            case "thursday", "r" -> THURSDAY;
            case "friday", "f" -> FRIDAY;
            case "saturday", "s" -> SATURDAY;
            case "tba" -> TBA;
            default -> null;
        };
    }

    /**
     * Convert a string of UH day codes to Day enums
     * <p>
     * ie MW -> Monday, Wednesday
     *
     * @param code Day code string
     * @return List of Days
     */
    public static List<Day> toDays(String code) {
        List<Day> days = new ArrayList<>();
        Day day = toDay(code);
        // Single day or 'tba'
        if (day != null) {
            days.add(day);
            return days;
        }
        for (int i = 0; i < code.length(); i++)
            days.add(Day.toDay(code.substring(i, i + 1)));
        while (days.remove(null)) ;    // remove nulls
        return days;
    }

    /**
     * @return Day of week
     */
    public int getDow() {
        return this.dow;
    }

    /**
     * Convert Day enum to UH Day code
     *
     * @return UH Day code
     */
    public String toCode() {
        switch (this) {
            case SUNDAY -> {
                return "U";
            }
            case MONDAY -> {
                return "M";
            }
            case TUESDAY -> {
                return "T";
            }
            case WEDNESDAY -> {
                return "W";
            }
            case THURSDAY -> {
                return "R";
            }
            case FRIDAY -> {
                return "F";
            }
            case SATURDAY -> {
                return "S";
            }
            default -> {
                return "TBA";
            }
        }
    }
}