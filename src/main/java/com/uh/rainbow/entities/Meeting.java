package com.uh.rainbow.entities;

import com.uh.rainbow.entities.timeblock.TimeBlock_I;

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
    private final String startTime;
    private final String startDate;
    private final String endTime;
    private final String endDate;
    private final String room;

    /**
     * Create new meeting
     *
     * @param day Day of Week
     * @param startTime Start time
     * @param startDate Start date
     * @param endTime End Time
     * @param endDate End date
     * @param room Room
     */
    private Meeting(Day day, String startTime, String startDate, String endTime, String endDate, String room) {
        this.day = day;
        this.startTime = startTime;
        this.startDate = startDate;
        this.endTime = endTime;
        this.endDate = endDate;
        this.room = room;
    }

    /**
     * @return Day of week meeting occurs on
     */
    public int getDow() {
        return this.day.getDow();
    }

    public Day getDay(){
        return this.day;
    }

    public String getstart_time(){
        return this.startTime;
    }

    public String getstart_date(){
        return this.startDate;
    }

    public String getend_time() {
        return this.endTime;
    }

    public String getend_date() {
        return this.endDate;
    }

    public String getroom() {
        return this.room;
    }


    /**
     * Create new meetings parsed from UH style input parameters
     *
     * @param dayString Day string formatted D*
     * @param timeString Time formatted HHmm-HHmm(?:a|p)
     * @param roomString Room name
     * @param dateString Date formatted DD/MM(?:|-DD/MM)
     * @return List of parsed meetings
     * @throws ParseException Fail to parse time or day block
     */
    public static List<Meeting> createMeetings(String dayString, String timeString, String roomString, String dateString) throws ParseException {
        List<Meeting> meetings = new ArrayList<>();

        List<Day> days = Day.toDays(dayString);

        TimeBlock_I tb = TimeBlock_I.createTimeBlock(timeString, dateString);
        days.forEach((day) -> meetings.add(
                new Meeting(day, tb.getStartTime(), tb. getStartDate(), tb.getEndTime(), tb.getEndDate(), roomString))
        );

        return meetings;
    }


}
