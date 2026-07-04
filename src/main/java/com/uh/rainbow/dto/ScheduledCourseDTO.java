package com.uh.rainbow.dto;

/**
 * DTO with some course metadata and a single section for the schedule
 *
 * @param subjectCode  Subject code
 * @param courseNumber Course number
 * @param name         Name of course
 * @param description  Description of course
 * @param credits      Total credits
 * @param section      Section of course scheduled
 */
public record ScheduledCourseDTO(String subjectCode, String courseNumber,
                                 String name, String description,
                                 int credits, SectionDTO section) {
}
