package com.uh.starlite.dto;

import com.uh.starlite.util.ScheduleCodec;

import java.util.Comparator;
import java.util.List;

/**
 * Create new Schedule DTO
 *
 * @param courses List of courses scheduled
 */
public record ScheduleDTO(String jsonURL, List<ScheduledCourseDTO> courses) {

    // comparator that sorts courses by the code, then number
    private static final Comparator<ScheduledCourseDTO> BY_COURSE_ID = Comparator
            .comparing(ScheduledCourseDTO::subjectCode)
            .thenComparing(ScheduledCourseDTO::courseNumber);   // not perfect since comparing strings

    // compact constructor - normalizes/sorts courses on construction
    public ScheduleDTO {
        courses = courses.stream()
                .sorted(BY_COURSE_ID)
                .toList();
    }

    /**
     * Build a ScheduleDTO, deriving the encoding from campus, term, and courses.
     *
     * @param publicEndpoint Endpoint to access the API at for callback
     * @param campusCode     Campus code the schedule belongs to
     * @param termCode       Term code the schedule belongs to
     * @param courses        Courses included in the schedule
     * @return A ScheduleDTO with courses and encoded version
     */
    public static ScheduleDTO of(String publicEndpoint, String campusCode, String termCode, List<ScheduledCourseDTO> courses) {
        String jsonURL = publicEndpoint + "/schedules/" + ScheduleCodec.encode(campusCode, termCode, courses) + "/json";
        return new ScheduleDTO(jsonURL, courses);
    }
}
