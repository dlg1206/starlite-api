package com.uh.rainbow.response;

import com.uh.rainbow.dto.schedule.ScheduleDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> ScheduleResponseDTO.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class ScheduleResponse extends Response {
    public final List<ScheduleDTO> schedules;

    /**
     * Create empty Schedule response
     */
    public ScheduleResponse() {
        this.schedules = new ArrayList<>();
    }

    /**
     * Create new Schedule response with list of valid schedules
     */
    public ScheduleResponse(List<ScheduleDTO> schedules) {
        this.schedules = schedules;
    }
}
