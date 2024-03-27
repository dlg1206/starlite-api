package com.uh.rainbow.entities;


import com.uh.rainbow.entities.timeblock.TimeBlock;
import com.uh.rainbow.entities.timeblock.simple.SimpleDate;
import com.uh.rainbow.entities.timeblock.simple.SimpleTime;

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
     * @return Day of week meeting occurs on
     */
    public int getDow() {
        return this.day.getDow();
    }

    public SimpleTime getStartTime() {
        return this.startTime;
    }

    public SimpleTime getEndTime() {
        return this.endTime;
    }

    public String getRoom() {
        return this.room;
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

}
