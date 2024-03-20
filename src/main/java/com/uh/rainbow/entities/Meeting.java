package com.uh.rainbow.entities;

import com.uh.rainbow.entities.timeblock.TimeBlock_I;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Meeting.java
 * <p>
 * <b>Description:</b>
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

    private Meeting(Day day, String startTime, String startDate, String endTime, String endDate, String room) {
        this.day = day;
        this.startTime = startTime;
        this.startDate = startDate;
        this.endTime = endTime;
        this.endDate = endDate;
        this.room = room;
    }

    public int getDow() {
        return this.day.getDow();
    }


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
