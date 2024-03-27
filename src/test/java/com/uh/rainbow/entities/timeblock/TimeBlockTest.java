package com.uh.rainbow.entities.timeblock;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>File:</b> TimeBlockTest.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class TimeBlockTest {

//    @Test
//    public void create_valid_TimeBlock_am_am(){
//        // Given
//        String timeString = "0900-1030a";
//        String dateString = "08/26-12/20";
//
//        try {
//            // When
//            TimeBlock_I timeBlock = TimeBlock_I.createTimeBlock(timeString, dateString);
//
//            // Then
//            assertEquals("09:00 AM", timeBlock.getStartTime());
//            assertEquals("08/26", timeBlock.getStartDate());
//            assertEquals("10:30 AM", timeBlock.getEndTime());
//            assertEquals("12/20", timeBlock.getEndDate());
//
//        } catch (Exception e){
//            fail(e);
//        }
//    }
//
//    @Test
//    public void create_valid_TimeBlock_pm_pm(){
//        // Given
//        String timeString = "0100-0200p";
//        String dateString = "08/26-12/20";
//
//        try {
//            // When
//            TimeBlock_I timeBlock = TimeBlock_I.createTimeBlock(timeString, dateString);
//
//            // Then
//            assertEquals("01:00 PM", timeBlock.getStartTime());
//            assertEquals("08/26", timeBlock.getStartDate());
//            assertEquals("02:00 PM", timeBlock.getEndTime());
//            assertEquals("12/20", timeBlock.getEndDate());
//
//        } catch (Exception e){
//            fail(e);
//        }
//    }
//
//    @Test
//    public void create_valid_TimeBlock_tba(){
//        // Given
//        String timeString = "TBA";
//        String dateString = "08/26-12/20";
//
//        try {
//            // When
//            TimeBlock_I timeBlock = TimeBlock_I.createTimeBlock(timeString, dateString);
//
//            // Then
//            assertInstanceOf(TBATimeBlock.class, timeBlock);
//            assertEquals("TBA", timeBlock.getStartTime());
//            assertEquals("08/26", timeBlock.getStartDate());
//            assertEquals("TBA", timeBlock.getEndTime());
//            assertEquals("12/20", timeBlock.getEndDate());
//
//        } catch (Exception e){
//            fail(e);
//        }
//    }
//
//    @Test
//    public void create_valid_TimeBlock_single_date(){
//        // Given
//        String timeString = "0900-1030a";
//        String dateString = "08/26";
//
//        try {
//            // When
//            TimeBlock_I timeBlock = TimeBlock_I.createTimeBlock(timeString, dateString);
//
//            // Then
//            assertEquals("09:00 AM", timeBlock.getStartTime());
//            assertEquals("08/26", timeBlock.getStartDate());
//            assertEquals("10:30 AM", timeBlock.getEndTime());
//            assertEquals("08/26", timeBlock.getEndDate());
//
//        } catch (Exception e){
//            fail(e);
//        }
//    }
//
//    @Test
//    public void create_invalid_TimeBlock_with_bad_time(){
//        // Given
//        String timeString = "foo";
//        String dateString = "08/26-12/20";
//
//        try {
//            // When
//            TimeBlock_I timeBlock = TimeBlock_I.createTimeBlock(timeString, dateString);
//
//            // Then
//            fail("Time was bad");
//
//        } catch (Exception e){
//            assertNotNull(e);
//        }
//    }
//
//    @Test
//    public void create_invalid_TimeBlock_with_bad_date(){
//        // todo
//    }
}
