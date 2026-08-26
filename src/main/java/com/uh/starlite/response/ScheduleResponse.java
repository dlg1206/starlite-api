package com.uh.starlite.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.starlite.dto.ScheduleDTO;

import java.util.Date;

/**
 * <b>File:</b> ScheduleResponse.java
 * <p>
 * <b>Description:</b> Response of a single schedule
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "schedule"})
public record ScheduleResponse(Date timestamp, ScheduleDTO schedule) {
    /**
     * Create new Schedule response with a single schedule
     */
    public ScheduleResponse(ScheduleDTO schedule) {
        this(new Date(), schedule);
    }
}
