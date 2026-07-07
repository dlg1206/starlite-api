package com.uh.starlite.entities;

import com.uh.starlite.enums.Day;

import java.time.Duration;
import java.time.LocalTime;

/**
 * <b>File:</b> TimeSpan.java
 * <p>
 * <b>Description:</b> Represents a span of time on a single day
 *
 * @author Derek Garcia
 */
public interface TimeSpan {

    /**
     * @return Day time span occurs on
     */
    Day getDay();

    /**
     * @return Start time of span
     */
    LocalTime getStartTime();

    /**
     * @return End time of span
     */
    LocalTime getEndTime();

    /**
     * @return If the span is async (Default: false)
     */
    default boolean isAsync() {
        return false;
    }

    /**
     * Get the buffer time (time elapsed between end of other meeting and start of this one) between meetings
     *
     * @param other Other time span
     * @return Buffer time in minutes
     */
    default int bufferTime(TimeSpan other) {
        return (int) Duration.between(other.getEndTime(), getStartTime()).toMinutes();
    }

    /**
     * Determine if this time span conflicts with another time span
     *
     * @param other Other time span to compare against
     * @return True if conflict, false if otherwise
     */
    default boolean conflictsWith(TimeSpan other) {
        // TBD days can't conflict
        if (this.getDay() == Day.TBD || other.getDay() == Day.TBD)
            return false;

        // Async days can't conflict
        if (isAsync() || other.isAsync())
            return false;

        // Can't conflict if on different days
        if (this.getDay() != other.getDay())
            return false;

        // can't check if time conflicts if times are null
        if (this.getStartTime() == null || this.getEndTime() == null || other.getStartTime() == null || other.getEndTime() == null)
            return false;

        // Conflict if this starts before other ends or ends after other starts
        return this.getStartTime().isBefore(other.getEndTime()) && other.getStartTime().isBefore(getEndTime());
    }

    /**
     * Determine if this time span conflicts with another time span with a buffer
     *
     * @param other      Other time span to compare against
     * @param bufferTime Minimum buffer time (in minutes) between time spans
     * @return True if conflict, false if otherwise
     */
    default boolean conflictsWith(TimeSpan other, int bufferTime) {
        // check if conflicts without buffer then check buffer
        return conflictsWith(other) || bufferTime(other) < bufferTime;
    }

}
