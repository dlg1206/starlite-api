package com.uh.rainbow.dto.course;


/**
 * Meeting DTO with section details
 *
 * @param day          Day of week meeting occurs
 * @param startTime    Start time of meeting in HHmm
 * @param endTime      End time of meeting in HHmm
 * @param buildingCode Building code
 * @param roomCode     Room code
 */
public record MeetingDTO(String day,
                         String startTime, String endTime,
                         String buildingCode, String roomCode) {
}
