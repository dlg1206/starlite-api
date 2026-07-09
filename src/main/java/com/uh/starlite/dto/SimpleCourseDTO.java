package com.uh.starlite.dto;

import java.util.List;

/**
 * Course DTO with only course details
 *
 * @param subjectCode       Subject code
 * @param courseNumber      Course number
 * @param name              Name of course
 * @param description       Description of course
 * @param credits           Total credits
 * @param gradingOptions    List of grading options available for this course
 * @param majorRestriction  If the selection is restricted to the major of the parent course
 * @param approvalAuthority Authority approval required to take the course
 * @param startDate         Start date of course
 * @param endDate           End date of course
 * @param numSections       Total sections for course
 */
public record SimpleCourseDTO(String subjectCode, String courseNumber, String name,
                              String description, String prereqDescription,
                              int credits, List<String> gradingOptions,
                              boolean majorRestriction, String approvalAuthority,
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
