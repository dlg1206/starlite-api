package com.uh.rainbow.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.rainbow.dto.course.ScheduledCourseDTO;

import java.util.List;

/**
 * <b>File:</b> ScheduleResponseDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "schedules"})
public class ScheduleResponse extends Response {
    public final List<List<ScheduledCourseDTO>> schedules;

    /**
     * Create new Schedule response with list of valid schedules
     */
    public ScheduleResponse(List<List<ScheduledCourseDTO>> schedules) {
        this.schedules = schedules;
    }
}
