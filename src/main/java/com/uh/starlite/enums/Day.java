package com.uh.starlite.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import net.fortuna.ical4j.model.WeekDay;

import java.time.DayOfWeek;
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

    /**
     * Convert Day to {@link DayOfWeek}
     *
     * @return {@link DayOfWeek} or null if TBD
     */
    public DayOfWeek toDayOfWeek() {
        return switch (this) {
            case SUN -> DayOfWeek.SUNDAY;
            case MON -> DayOfWeek.MONDAY;
            case TUE -> DayOfWeek.TUESDAY;
            case WED -> DayOfWeek.WEDNESDAY;
            case THU -> DayOfWeek.THURSDAY;
            case FRI -> DayOfWeek.FRIDAY;
            case SAT -> DayOfWeek.SATURDAY;
            default -> null;
        };
    }

    /**
     * Convert Day to {@link WeekDay}
     *
     * @return {@link WeekDay} or null if TBD
     */
    public WeekDay toWeekDay() {
        return switch (this) {
            case SUN -> WeekDay.SU;
            case MON -> WeekDay.MO;
            case TUE -> WeekDay.TU;
            case WED -> WeekDay.WE;
            case THU -> WeekDay.TH;
            case FRI -> WeekDay.FR;
            case SAT -> WeekDay.SA;
            default -> null;
        };
    }
}
