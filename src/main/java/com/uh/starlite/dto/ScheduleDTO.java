package com.uh.starlite.dto;

import com.uh.starlite.util.ScheduleCodec;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Comparator;
import java.util.List;

import static com.uh.starlite.util.Uri.scheduleIcs;
import static com.uh.starlite.util.Uri.scheduleJson;

/**
 * Create new Schedule DTO
 *
 * @param jsonURL URL to get this schedule
 * @param icsURL  URL to the ical version of this schedule
 * @param courses List of courses scheduled
 */
public record ScheduleDTO(String jsonURL, String icsURL, List<ScheduledCourseDTO> courses) {

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
     * @param campusCode Campus code the schedule belongs to
     * @param termCode   Term code the schedule belongs to
     * @param courses    Courses included in the schedule
     * @return A ScheduleDTO with courses and encoded version
     */
    public static ScheduleDTO of(String campusCode, String termCode, List<ScheduledCourseDTO> courses) {
        String encodedSchedule = ScheduleCodec.encode(campusCode, termCode, courses);
        return new ScheduleDTO(scheduleJson(encodedSchedule), scheduleIcs(encodedSchedule), courses);
    }
}
