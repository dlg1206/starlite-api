package com.uh.rainbow.entities;

import com.uh.rainbow.enums.Day;

import java.time.LocalTime;

/**
 * Block of reserved time
 *
 * @param day       Day block occurs on
 * @param startTime Start of block
 * @param endTime   End of block
 */
public record ReservedTime(Day day, LocalTime startTime, LocalTime endTime) implements TimeSpan {

    /**
     * @return Day time span occurs on
     */
    @Override
    public Day getDay() {
        return day;
    }

    /**
     * @return Start time of reserved time
     */
    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @return End of reserved time
     */
    @Override
    public LocalTime getEndTime() {
        return endTime;
    }
}
