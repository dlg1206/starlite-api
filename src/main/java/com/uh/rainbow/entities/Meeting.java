package com.uh.rainbow.entities;


import com.uh.rainbow.entities.time.TimeBlock;
import com.uh.rainbow.entities.time.simple.SimpleDate;
import com.uh.rainbow.entities.time.simple.SimpleTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Meeting.java
 * <p>
 * <b>Description:</b> Representation of a meeting period
 *
 * @author Derek Garcia
 */
public class Meeting {
    private final Day day;
    private final SimpleTime startTime;
    private final SimpleTime endTime;
    private final SimpleDate startDate;
    private final SimpleDate endDate;

    private final String room;

    /**
     * Create new meeting
     *
     * @param day  Day of Week
     * @param room Room
     */
    private Meeting(Day day, SimpleTime startTime, SimpleTime endTime, SimpleDate startDate, SimpleDate endDate, String room) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.room = room;
    }

    /**
     * Determine if this meeting conflicts with another meeting
     *
     * @param other Other meeting to compare against
     * @return True if conflict, false if otherwise
     */
    public boolean conflictsWith(Meeting other) {
        // TBA days can't conflict
        if (this.day == Day.TBA || other.day == Day.TBA)
            return false;

        // Can't conflict if on different days
        if (this.day != other.day)
            return false;

        // todo handle single day meetings

        // Conflict if times overlap
        return this.startTime.beforeOrEqual(other.endTime) == 1 && this.endTime.afterOrEqual(other.startTime) == 1;

        // No conflicts
    }

    /**
     * Create new meetings parsed from UH style input parameters
     *
     * @param dayString  Day string formatted D*
     * @param timeString Time formatted HHmm-HHmm(?:a|p)
     * @param roomString Room name
     * @param dateString Date formatted DD/MM(?:|-DD/MM)
     * @return List of parsed meetings
     * @throws ParseException Fail to parse time or day block
     */
    public static List<Meeting> createMeetings(String dayString, String timeString, String roomString, String dateString) throws ParseException {
        List<Meeting> meetings = new ArrayList<>();

        List<Day> days = Day.toDays(dayString);

        TimeBlock tb = new TimeBlock(timeString, dateString);
        days.forEach((day) -> meetings.add(
                new Meeting(day, tb.getStartTime(), tb.getEndTime(), tb.getStartDate(), tb.getEndDate(), roomString))
        );

        return meetings;
    }

    /**
     * @return Day of week meeting occurs on
     */
    public int getDow() {
        return this.day.getDow();
    }

    /**
     * @return Day of Week
     */
    public Day getDay() {
        return this.day;
    }

    /**
     * @return Start Time
     */
    public SimpleTime getStartTime() {
        return this.startTime;
    }

    /**
     * @return End Time
     */
    public SimpleTime getEndTime() {
        return this.endTime;
    }

    /**
     * @return Meeting Room
     */
    public String getRoom() {
        return this.room;
    }

    /**
     * @return Start Date of recurring meeting
     */
    public SimpleDate getStartDate() {
        return this.startDate;
    }

    /**
     * @return End Date of recurring meeting
     */
    public SimpleDate getEndDate() {
        return this.endDate;
    }

}
