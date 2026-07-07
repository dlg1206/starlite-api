package com.uh.starlite.entities;


import com.uh.starlite.dto.MeetingDTO;
import com.uh.starlite.enums.Day;

import java.time.LocalTime;
import java.util.Objects;

/**
 * <b>File:</b> Meeting.java
 * <p>
 * <b>Description:</b> Representation of a meeting block
 *
 * @author Derek Garcia
 */
public record Meeting(Day day, LocalTime startTime, LocalTime endTime, String buildingCode,
                      String roomCode) implements TimeSpan {

    /**
     * @return If this meeting is online
     */
    public boolean isOnline() {
        return buildingCode != null && buildingCode.equals("ONLINE");
    }

    /**
     * Convert this meeting to a DTO
     *
     * @return {@link MeetingDTO}
     */
    public MeetingDTO toMeetingDTO() {
        return new MeetingDTO(day, startTime, endTime, buildingCode, roomCode);
    }

    /**
     * @return If this meeting is async
     */
    @Override
    public boolean isAsync() {
        return roomCode != null && roomCode.equals("ASYNC");
    }

    /**
     * @return Day meeting occurs on
     */
    @Override
    public Day getDay() {
        return day;
    }

    /**
     * @return Start time of meeting
     */
    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @return End time of meeting
     */
    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return day == meeting.day && Objects.equals(startTime, meeting.startTime) && Objects.equals(endTime, meeting.endTime) && Objects.equals(buildingCode, meeting.buildingCode) && Objects.equals(roomCode, meeting.roomCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime, endTime, buildingCode, roomCode);
    }
}
