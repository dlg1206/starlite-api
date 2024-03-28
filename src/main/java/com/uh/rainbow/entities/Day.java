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

    /**
     * Convert UH Day code to Day enum
     *
     * @param code string code ID
     * @return Day Enum, null if not
     */
    private static Day toDay(String code) {
        switch (code.toLowerCase()) {
            case "u" -> {
                return SUNDAY;
            }
            case "m" -> {
                return MONDAY;
            }
            case "t" -> {
                return TUESDAY;
            }
            case "w" -> {
                return WEDNESDAY;
            }
            case "r" -> {
                return THURSDAY;
            }
            case "f" -> {
                return FRIDAY;
            }
            case "s" -> {
                return SATURDAY;
            }
            case "tba" -> {
                return TBA;
            }
            default -> {
                return null;
            }
        }
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
}