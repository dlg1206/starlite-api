package com.uh.starlite.dto;

import java.util.List;

/**
 * Create new Schedule DTO
 *
 * @param courses List of courses scheduled
 */
public record ScheduleDTO(List<ScheduledCourseDTO> courses) {
}
