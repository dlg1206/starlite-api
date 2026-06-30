package com.uh.rainbow.dto.course;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Course DTO with only course details
 *
 * @param subjectCode  Subject code
 * @param courseNumber Course number
 * @param name         Name of course
 * @param description  Description of course
 * @param credits      Total credits
 * @param attributes   Course attributes
 * @param descriptions Additional course descriptions
 * @param notes        Additional notes
 * @param startDate    Start date of course
 * @param endDate      End date of course
 * @param numSections  Total sections for course
 */
@JsonPropertyOrder({"subjectCode", "courseNumber"})
public record SimpleCourseDTO(String subjectCode, String courseNumber, String name,
                              String description, int credits,
                              List<String> attributes, List<String> descriptions, List<String> notes,
                              String startDate, String endDate,
                              int numSections) implements CourseDTO {
}
