package com.uh.rainbow.dto.schedule;

/**
 * <b>File:</b> ScheduleMeetingDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public record ScheduleMeetingDTO(String course, String cid, String sid, int crn, String instructor, String room, String start_time, String end_time, String url) {

}
