package com.uh.rainbow.dto.meeting;

/**
 * <b>File:</b> MeetingDTO.java
 * <p>
 * <b>Description:</b> MeetingDTO
 *
 * @author Derek Garcia
 */
public record MeetingDTO(String day, String room, String start_time, String end_time, String start_date,
                         String end_date) {
}
