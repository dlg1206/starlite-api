package com.uh.starlite.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Course record for jsonl
 *
 * @param courseUUID        UUID for course
 * @param campusCode        Campus code
 * @param termCode          Term code
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
 */
public record CourseRecord(String courseUUID,
                           String campusCode, String termCode, String subjectCode,
                           String courseNumber, String name,
                           String description, String prereqDescription,
                           int credits, List<String> gradingOptions,
                           boolean majorRestriction, String approvalAuthority,
                           LocalDate startDate, LocalDate endDate) {

}
