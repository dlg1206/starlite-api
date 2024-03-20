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
    private final String start;
    private final String end;
    private final String room;

    private Meeting(Day day, String start, String end, String room) {
        this.day = day;
        this.start = start;
        this.end = end;
        this.room = room;
    }

    public int getDow() {
        return this.day.getDow();
    }


    public static List<Meeting> createMeetings(String dayString, String timeString, String roomString) throws ParseException {
        List<Meeting> meetings = new ArrayList<>();

        List<Day> days = Day.toDays(dayString);

        TimeBlock_I tb = TimeBlock_I.createTimeBlock(timeString);
        days.forEach((day) -> meetings.add(new Meeting(day, tb.getStart(), tb.getEnd(), roomString)));


        return meetings;
    }


}
