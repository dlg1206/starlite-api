package com.uh.starlite.entities;

import com.uh.starlite.dto.MeetingDTO;
import com.uh.starlite.enums.Day;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Objects;

/**
 * <b>File:</b> Meeting.java
 * <p>
 * <b>Description:</b> Representation of a meeting block
 *
 * @author Derek Garcia
 */
public class Meeting implements TimeSpan {


    private final Day day;
    @Getter
    private final boolean isOnline;
    private final boolean isAsync;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String buildingCode;
    private final String roomCode;

    /**
     * Create a new meeting
     *
     * @param day {@link Day} of week meeting occurs on
     * @param startTime Start time of meeting
     * @param endTime End time of meeting
     * @param buildingCode Building code
     * @param roomCode Room code
     */
    public Meeting(Day day, LocalTime startTime, LocalTime endTime, String buildingCode, String roomCode){
        this.day = day;
        this.isOnline = buildingCode != null && buildingCode.equals("ONLINE");
        this.isAsync = roomCode != null && roomCode.equals("ASYNC");
        this.startTime = startTime;
        this.endTime = endTime;
        // only add codes if not online / async
        this.buildingCode = this.isOnline ? null : buildingCode;
        this.roomCode = this.isAsync ? null : roomCode;
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
        return isAsync;
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
