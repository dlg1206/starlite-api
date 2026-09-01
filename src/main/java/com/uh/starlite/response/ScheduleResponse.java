package com.uh.starlite.response;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.uh.starlite.dto.ScheduleDTO;

import java.time.Instant;

/**
 * <b>File:</b> ScheduleResponse.java
 * <p>
 * <b>Description:</b> Response of a single schedule
 *
 * @author Derek Garcia
 */
@JsonPropertyOrder({"timestamp", "schedule"})
public record ScheduleResponse(Instant timestamp, ScheduleDTO schedule) {
    /**
     * Create new Schedule response with a single schedule
     */
    public ScheduleResponse(ScheduleDTO schedule) {
        this(Instant.now(), schedule);
    }
}
