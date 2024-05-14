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
    public void create_valid_meeting_with_simple_day() {
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
        } catch (ParseException e) {
            fail(e);
        }
    }

    @Test
    public void create_valid_meeting_with_complex_day() {
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
        } catch (ParseException e) {
            fail(e);
        }
    }

    @Test
    public void a_start_before_and_end_before_b_start_and_end() {
        /*
        [ a ]
              [ b ]
         */
        try {
            // Given
            Meeting a = Meeting.createMeetings("M", "0900-1000a", "foo", "10/1-10/30").get(0);
            Meeting b = Meeting.createMeetings("M", "1100-1200p", "foo", "10/1-10/30").get(0);

            // When

            // Then
            assertFalse(a.conflictsWith(b));

        } catch (ParseException e) {
            fail(e);
        }

    }

    @Test
    public void a_end_after_b_start() {
        /*
        [ a ]
           [ b ]
        */
        try {
            // Given
            Meeting a = Meeting.createMeetings("M", "0900-1000a", "foo", "10/1-10/30").get(0);
            Meeting b = Meeting.createMeetings("M", "0930-1200p", "foo", "10/1-10/30").get(0);

            // When

            // Then
            assertTrue(a.conflictsWith(b));

        } catch (ParseException e) {
            fail(e);
        }
    }

    @Test
    public void a_start_before_b_start_and_end_after_b_end() {
        /*
        [   a   ]
          [ b ]
         */
        try {
            // Given
            Meeting a = Meeting.createMeetings("M", "0900-0100p", "foo", "10/1-10/30").get(0);
            Meeting b = Meeting.createMeetings("M", "1100-1200p", "foo", "10/1-10/30").get(0);

            // When

            // Then
            assertTrue(a.conflictsWith(b));

        } catch (ParseException e) {
            fail(e);
        }

    }

    @Test
    public void a_start_before_b_end() {
        /*
            [ a ]
          [ b ]
        */
        try {
            // Given
            Meeting a = Meeting.createMeetings("M", "1130-0100p", "foo", "10/1-10/30").get(0);
            Meeting b = Meeting.createMeetings("M", "1100-1200p", "foo", "10/1-10/30").get(0);

            // When

            // Then
            assertTrue(a.conflictsWith(b));

        } catch (ParseException e) {
            fail(e);
        }
    }

    @Test
    public void a_start_before_and_end_after_b_start() {
        /*
              [ a ]
        [ b ]
        */
        try {
            // Given
            Meeting a = Meeting.createMeetings("M", "1230-0100p", "foo", "10/1-10/30").get(0);
            Meeting b = Meeting.createMeetings("M", "1100-1200p", "foo", "10/1-10/30").get(0);

            // When

            // Then
            assertFalse(a.conflictsWith(b));

        } catch (ParseException e) {
            fail(e);
        }
    }


    @Test
    public void a_overlap_with_b_on_different_days() {
        try {
            // Given
            Meeting a = Meeting.createMeetings("M", "1100-1200p", "foo", "10/1-10/30").get(0);
            Meeting b = Meeting.createMeetings("T", "1100-1200p", "foo", "10/1-10/30").get(0);

            // When

            // Then
            assertFalse(a.conflictsWith(b));

        } catch (ParseException e) {
            fail(e);
        }
    }

}
