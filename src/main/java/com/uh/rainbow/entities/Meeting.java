package com.uh.rainbow.entities;


import com.uh.rainbow.dto.meeting.MeetingDTO;
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
     * Convert meeting into DTO
     *
     * @return MeetingDTO
     */
    public MeetingDTO toDTO() {
        return new MeetingDTO(
                this.day.toString(),
                this.room,
                this.startTime.toString(),
                this.endTime.toString(),
                this.startDate.toString(),
                this.endDate.toString()
        );
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
