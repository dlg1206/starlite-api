package com.uh.rainbow.dto.course;

import java.util.List;

/**
 * Course DTO with course and section details
 *
 * @param subjectCode  Subject code
 * @param courseNumber Course number
 * @param name         Name of course
 * @param description  Description of course
 * @param credits      Total credits
 * @param startDate    Start date of course
 * @param endDate      End date of course
 * @param sections     List of sections of the course
 */
public record DetailedCourseDTO(String subjectCode, String courseNumber, String name,
                                String description, int credits,
                                String startDate, String endDate,
                                List<SectionDTO> sections) implements CourseDTO {

    /**
     * @return Course subject code
     */
    @Override
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * @return Get course number
     */
    @Override
    public String getCourseNumber() {
        return courseNumber;
    }
}