package com.uh.rainbow.entities;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>File:</b> MeetingTest.java
 * <p>
 * <b>Description:</b> Unit Tests for meeting
 *
 * @author Derek Garcia
 */
public class MeetingTest {

    @Test
    public void create_valid_meeting_with_simple_day(){
        // Given
        String dayString = "M";
        String timeString = "0900-1030a";
        String dateString = "08/26-12/20";
        String roomString = "SHED";

        try {
            // When
            List<Meeting> meetings = Meeting.createMeetings(dayString, timeString, roomString, dateString);

            // Then
            assertEquals(1, meetings.size());
            assertEquals(Day.MONDAY.getDow(), meetings.get(0).getDow());
        } catch (ParseException e){
            fail(e);
        }
    }

    @Test
    public void create_valid_meeting_with_complex_day(){
        // Given
        String dayString = "MW";
        String timeString = "0900-1030a";
        String dateString = "08/26-12/20";
        String roomString = "SHED";

        try {
            // When
            List<Meeting> meetings = Meeting.createMeetings(dayString, timeString, roomString, dateString);

            // Then
            assertEquals(2, meetings.size());
            assertEquals(Day.MONDAY.getDow(), meetings.get(0).getDow());
            assertEquals(Day.WEDNESDAY.getDow(), meetings.get(1).getDow());
        } catch (ParseException e){
            fail(e);
        }
    }
}
