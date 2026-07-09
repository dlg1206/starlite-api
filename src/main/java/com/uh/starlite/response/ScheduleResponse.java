package com.uh.starlite.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.starlite.dto.ScheduledCourseDTO;

import java.util.Date;
import java.util.List;

/**
 * <b>File:</b> ScheduleResponse.java
 * <p>
 * <b>Description:</b> Response of all schedules
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "schedules"})
public record ScheduleResponse(Date timestamp, List<List<ScheduledCourseDTO>> schedules) {
    /**
     * Create new Schedule response with list of valid schedules
     */
    public ScheduleResponse(List<List<ScheduledCourseDTO>> schedules) {
        this(new Date(), schedules);
    }
}
