package com.uh.starlite.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.starlite.dto.ScheduleDTO;

import java.util.Date;
import java.util.List;

/**
 * <b>File:</b> ScheduleResponse.java
 * <p>
 * <b>Description:</b> Response of all generated schedules
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "schedules"})
public record SchedulesResponse(Date timestamp, List<ScheduleDTO> schedules) {
    /**
     * Create new Schedule response with list of valid schedules
     */
    public SchedulesResponse(List<ScheduleDTO> schedules) {
        this(new Date(), schedules);
    }
}
