package com.uh.rainbow.entities;

import com.uh.rainbow.dto.meeting.MeetingDTO;
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

    @Test
    public void a_start_before_and_end_before_b_start_and_end(){
        /*
        [ a ]
              [ b ]
         */
        try{
            // Given
            Meeting a = new Meeting(new MeetingDTO("M", "foo", "09:00 am", "10:00 am", "10/1", "10/30"));
            Meeting b = new Meeting(new MeetingDTO("M", "foo", "11:00 am", "12:00 pm", "10/1", "10/30"));

            // When

            // Then
            assertFalse(a.conflictsWith(b));

        } catch (ParseException e){
            fail(e);
        }

    }
    @Test
    public void a_end_after_b_start(){
        /*
        [ a ]
           [ b ]
        */
        try{
            // Given
            Meeting a = new Meeting(new MeetingDTO("M", "foo", "09:00 am", "10:00 am", "10/1", "10/30"));
            Meeting b = new Meeting(new MeetingDTO("M", "foo", "9:30 am", "12:00 pm", "10/1", "10/30"));

            // When

            // Then
            assertTrue(a.conflictsWith(b));

        } catch (ParseException e){
            fail(e);
        }
    }
    @Test
    public void a_start_before_b_start_and_end_after_b_end(){
        /*
        [   a   ]
          [ b ]
         */
        try{
            // Given
            Meeting a = new Meeting(new MeetingDTO("M", "foo", "09:00 am", "01:00 pm", "10/1", "10/30"));
            Meeting b = new Meeting(new MeetingDTO("M", "foo", "11:00 am", "12:00 pm", "10/1", "10/30"));

            // When

            // Then
            assertTrue(a.conflictsWith(b));

        } catch (ParseException e){
            fail(e);
        }

    }
    @Test
    public void a_start_before_b_end(){
        /*
            [ a ]
          [ b ]
        */
        try{
            // Given
            Meeting a = new Meeting(new MeetingDTO("M", "foo", "11:30 am", "01:00 pm", "10/1", "10/30"));
            Meeting b = new Meeting(new MeetingDTO("M", "foo", "11:00 am", "12:00 pm", "10/1", "10/30"));

            // When

            // Then
            assertTrue(a.conflictsWith(b));

        } catch (ParseException e){
            fail(e);
        }
    }
    @Test
    public void a_start_before_and_end_after_b_start(){
        /*
              [ a ]
        [ b ]
        */
        try{
            // Given
            Meeting a = new Meeting(new MeetingDTO("M", "foo", "12:30 pm", "01:00 pm", "10/1", "10/30"));
            Meeting b = new Meeting(new MeetingDTO("M", "foo", "11:00 am", "12:00 pm", "10/1", "10/30"));

            // When

            // Then
            assertFalse(a.conflictsWith(b));

        } catch (ParseException e){
            fail(e);
        }
    }


    @Test
    public void a_overlap_with_b_on_different_days(){
        try{
            // Given
            Meeting a = new Meeting(new MeetingDTO("M", "foo", "11:00 am", "12:00 pm", "10/1", "10/30"));
            Meeting b = new Meeting(new MeetingDTO("T", "foo", "11:00 am", "12:00 pm", "10/1", "10/30"));

            // When

            // Then
            assertFalse(a.conflictsWith(b));

        } catch (ParseException e){
            fail(e);
        }
    }

}
