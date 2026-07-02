package com.uh.rainbow.entities;


import com.uh.rainbow.dto.course.MeetingDTO;
import com.uh.rainbow.enums.Day;
import lombok.Getter;

import java.time.LocalTime;

/**
 * <b>File:</b> Meeting.java
 * <p>
 * <b>Description:</b> Representation of a meeting block
 *
 * @author Derek Garcia
 */
public class Meeting {
    @Getter
    private final Day day;
    @Getter
    private final LocalTime startTime;
    @Getter
    private final LocalTime endTime;

    private final String buildingCode;
    private final String roomCode;


    /**
     * Create new meeting
     *
     * @param day      Day of Week
     * @param roomCode Room
     */
    public Meeting(Day day, LocalTime startTime, LocalTime endTime, String buildingCode, String roomCode) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.buildingCode = buildingCode;
        this.roomCode = roomCode;
    }

    /**
     * Determine if this meeting conflicts with another meeting
     *
     * @param other Other meeting to compare against
     * @return True if conflict, false if otherwise
     */
    public boolean conflictsWith(Meeting other) {
        // todo
        return true;
        // TBA days can't conflict
//        if (this.day == Day.TBA || other.day == Day.TBA)
//            return false;
//
//        // Can't conflict if on different days
//        if (this.day != other.day)
//            return false;
//
//        // todo handle single day meetings
//
//        // Conflict if times overlap
//        return this.startTime.beforeOrEqual(other.endTime) == 1 && this.endTime.afterOrEqual(other.startTime) == 1;

        // No conflicts
    }

    /**
     * @return If this meeting is online
     */
    public boolean isOnline() {
        return buildingCode.equals("ONLINE");
    }

    /**
     * @return If this meeting is async
     */
    public boolean isAsync() {
        return roomCode.equals("ASYNC");
    }


    /**
     * Convert this meeting to a DTO
     *
     * @return {@link MeetingDTO}
     */
    public MeetingDTO toMeetingDTO() {
        return new MeetingDTO(day, startTime, endTime, buildingCode, roomCode);
    }

}
