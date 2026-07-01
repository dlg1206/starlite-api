package com.uh.rainbow.dto.course;

/**
 * Course DTO with only course details
 *
 * @param subjectCode  Subject code
 * @param courseNumber Course number
 * @param name         Name of course
 * @param description  Description of course
 * @param credits      Total credits
 * @param startDate    Start date of course
 * @param endDate      End date of course
 * @param numSections  Total sections for course
 */
public record SimpleCourseDTO(String subjectCode, String courseNumber, String name,
                              String description, int credits,
                              String startDate, String endDate,
                              int numSections) implements CourseDTO {
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
